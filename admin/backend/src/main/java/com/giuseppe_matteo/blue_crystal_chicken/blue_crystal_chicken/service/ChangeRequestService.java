package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config.RabbitConfig;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CreateChangeRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ChangeRequestResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestStatus;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestType;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.ChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Flusso di approvazione delle richieste del Manager.
 *
 *  Manager  ──POST /api/requests──►  ChangeRequest(PENDING)  ──RabbitMQ "request.created"──►  Admin
 *  Admin    ──PUT  .../approve────►  esegue la create reale   ──RabbitMQ "request.reply.*"──►  Manager
 *
 * Finché è PENDING non viene scritto nulla sul dominio reale.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeRequestService {

    private final ChangeRequestRepository repository;
    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final OfferService offerService;
    private final MenuService menuService;
    private final NotificationPublisher notificationPublisher;
    private final RabbitTemplate rabbitTemplate;

    // ── Creazione (Manager) ──────────────────────────────────────────────────
    @Transactional
    public ChangeRequestResponse create(CreateChangeRequest dto, Long userId, String email,
                                        Long locationId, String locationName) {
        ChangeRequestType type;
        try {
            type = ChangeRequestType.valueOf(dto.getType());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo richiesta non valido: " + dto.getType());
        }

        String payloadJson;
        try {
            payloadJson = dto.getPayload() != null ? objectMapper.writeValueAsString(dto.getPayload()) : "{}";
        } catch (Exception e) {
            throw new IllegalArgumentException("Payload non serializzabile: " + e.getMessage());
        }

        String summary = (dto.getSummary() != null && !dto.getSummary().isBlank())
                ? dto.getSummary()
                : defaultSummary(type, dto.getPayload());

        ChangeRequestEntity entity = ChangeRequestEntity.builder()
                .type(type)
                .status(ChangeRequestStatus.PENDING)
                .targetId(dto.getTargetId())
                .payload(payloadJson)
                .summary(summary)
                .requestedById(userId)
                .requestedByEmail(email)
                .locationId(locationId)
                .locationName(locationName)
                .build();

        entity = repository.save(entity);

        // RabbitMQ: coda dedicata richieste (Manager -> Admin)
        publishSilently("request.created", toResponse(entity));
        // Notifica visibile nel log (l'Admin vede "nuova richiesta")
        notificationPublisher.publish("event.request.created", "REQUEST_CREATED", "INFO", "Richieste",
                "Nuova richiesta dal manager",
                summary + (locationName != null ? " — sede " + locationName : ""));

        log.info("ChangeRequest #{} creata da {} (sede {})", entity.getId(), email, locationName);
        return toResponse(entity);
    }

    // ── Lettura ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ChangeRequestResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestResponse> findByStatus(ChangeRequestStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestResponse> findByUser(Long userId) {
        return repository.findByRequestedByIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestResponse> findByLocation(Long locationId) {
        return repository.findByLocationIdOrderByCreatedAtDesc(locationId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Approvazione (Admin) — esegue la mutazione reale ─────────────────────
    @Transactional
    public ChangeRequestResponse approve(Long id, Long adminId) {
        ChangeRequestEntity req = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));
        if (req.getStatus() != ChangeRequestStatus.PENDING) {
            throw new IllegalStateException("La richiesta è già stata gestita (" + req.getStatus() + ")");
        }

        String resultMessage;
        try {
            resultMessage = execute(req);
        } catch (Exception e) {
            // L'esecuzione è fallita: la richiesta resta PENDING, l'admin può ritentare.
            log.error("Esecuzione richiesta #{} fallita: {}", id, e.getMessage());
            throw new IllegalStateException("Esecuzione fallita: " + e.getMessage());
        }

        req.setStatus(ChangeRequestStatus.APPROVED);
        req.setResolvedById(adminId);
        req.setResolvedAt(LocalDateTime.now());
        req.setResultMessage(resultMessage);
        req = repository.save(req);

        publishSilently("request.reply.approved", toResponse(req));
        notificationPublisher.publish("event.request.resolved", "REQUEST_APPROVED", "SUCCESS", "Richieste",
                "Richiesta approvata", req.getSummary() + " — " + resultMessage);

        log.info("ChangeRequest #{} APPROVATA da admin {}", id, adminId);
        return toResponse(req);
    }

    // ── Rifiuto (Admin) ──────────────────────────────────────────────────────
    @Transactional
    public ChangeRequestResponse reject(Long id, Long adminId, String note) {
        ChangeRequestEntity req = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata: " + id));
        if (req.getStatus() != ChangeRequestStatus.PENDING) {
            throw new IllegalStateException("La richiesta è già stata gestita (" + req.getStatus() + ")");
        }

        req.setStatus(ChangeRequestStatus.REJECTED);
        req.setResolvedById(adminId);
        req.setResolvedAt(LocalDateTime.now());
        req.setResolutionNote(note);
        req = repository.save(req);

        publishSilently("request.reply.rejected", toResponse(req));
        notificationPublisher.publish("event.request.resolved", "REQUEST_REJECTED", "WARNING", "Richieste",
                "Richiesta rifiutata", req.getSummary() + (note != null ? " — " + note : ""));

        log.info("ChangeRequest #{} RIFIUTATA da admin {}", id, adminId);
        return toResponse(req);
    }

    // ── Esecuzione concreta in base al tipo ──────────────────────────────────
    private String execute(ChangeRequestEntity req) throws Exception {
        Long targetId = req.getTargetId();
        switch (req.getType()) {
            case CREATE_PRODUCT: {
                ProductRequest pr = objectMapper.readValue(req.getPayload(), ProductRequest.class);
                ProductResponse created = productService.createProduct(pr);
                return "Prodotto creato: " + created.getName() + " (id " + created.getId() + ")";
            }
            case UPDATE_PRODUCT: {
                requireTarget(targetId);
                ProductRequest pr = objectMapper.readValue(req.getPayload(), ProductRequest.class);
                ProductResponse updated = productService.updateProduct(targetId, pr);
                return "Prodotto aggiornato: " + updated.getName() + " (id " + updated.getId() + ")";
            }
            case DELETE_PRODUCT: {
                requireTarget(targetId);
                productService.deleteProduct(targetId);
                return "Prodotto eliminato (id " + targetId + ")";
            }
            case CREATE_MENU: {
                MenuRequest mr = objectMapper.readValue(req.getPayload(), MenuRequest.class);
                MenuResponse created = menuService.createMenu(mr);
                return "Menu creato: " + created.getName() + " (id " + created.getId() + ")";
            }
            case UPDATE_MENU: {
                requireTarget(targetId);
                MenuRequest mr = objectMapper.readValue(req.getPayload(), MenuRequest.class);
                MenuResponse updated = menuService.update(targetId, mr);
                return "Menu aggiornato: " + updated.getName() + " (id " + updated.getId() + ")";
            }
            case DELETE_MENU: {
                requireTarget(targetId);
                menuService.delete(targetId);
                return "Menu eliminato (id " + targetId + ")";
            }
            case CREATE_OFFER: {
                OfferRequest or = objectMapper.readValue(req.getPayload(), OfferRequest.class);
                OfferResponse created = offerService.create(or);
                return "Offerta creata: " + created.getName() + " (id " + created.getId() + ")";
            }
            default:
                throw new IllegalStateException("Tipo non gestito: " + req.getType());
        }
    }

    private void requireTarget(Long targetId) {
        if (targetId == null) throw new IllegalArgumentException("targetId mancante per UPDATE/DELETE");
    }

    private String defaultSummary(ChangeRequestType type, Object payload) {
        String name = "(senza nome)";
        if (payload instanceof java.util.Map<?, ?> m && m.get("name") != null) {
            name = String.valueOf(m.get("name"));
        }
        return switch (type) {
            case CREATE_PRODUCT -> "Nuovo prodotto: " + name;
            case UPDATE_PRODUCT -> "Modifica prodotto: " + name;
            case DELETE_PRODUCT -> "Elimina prodotto: " + name;
            case CREATE_MENU -> "Nuovo menu: " + name;
            case UPDATE_MENU -> "Modifica menu: " + name;
            case DELETE_MENU -> "Elimina menu: " + name;
            case CREATE_OFFER -> "Nuova offerta: " + name;
        };
    }

    private void publishSilently(String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, payload);
        } catch (Exception e) {
            log.warn("Pubblicazione richiesta fallita [{}]: {}", routingKey, e.getMessage());
        }
    }

    private ChangeRequestResponse toResponse(ChangeRequestEntity e) {
        return ChangeRequestResponse.builder()
                .id(e.getId())
                .type(e.getType() != null ? e.getType().name() : null)
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .targetId(e.getTargetId())
                .payload(e.getPayload())
                .summary(e.getSummary())
                .requestedById(e.getRequestedById())
                .requestedByEmail(e.getRequestedByEmail())
                .locationId(e.getLocationId())
                .locationName(e.getLocationName())
                .resolvedById(e.getResolvedById())
                .resolutionNote(e.getResolutionNote())
                .resultMessage(e.getResultMessage())
                .createdAt(e.getCreatedAt())
                .resolvedAt(e.getResolvedAt())
                .build();
    }
}
