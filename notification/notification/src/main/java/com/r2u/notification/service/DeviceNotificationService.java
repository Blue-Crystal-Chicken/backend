package com.r2u.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.r2u.notification.dto.mapper.DeviceNotificationMapper;
import com.r2u.notification.dto.request.DeviceNotificationRequest;
import com.r2u.notification.dto.response.DeviceNotificationResponse;
import com.r2u.notification.entity.enums.Status;
import com.r2u.notification.entity.model.DeviceEntity;
import com.r2u.notification.entity.model.DeviceNotification;
import com.r2u.notification.entity.model.NotificationEntity;
import com.r2u.notification.entity.model.UserDevice;
import com.r2u.notification.repository.DeviceNotificationRepository;
import com.r2u.notification.repository.DeviceRepository;
import com.r2u.notification.repository.NotificationRepository;
import com.r2u.notification.repository.UserDeviceRepository;
import com.google.firebase.messaging.Notification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceNotificationService {
    
    private final DeviceNotificationRepository deviceNotificationRepository;
    private final DeviceNotificationMapper deviceNotificationMapper;
    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Transactional
    public DeviceNotificationResponse createDeviceNotification(DeviceNotificationRequest request) {

        NotificationEntity notification = notificationRepository.findById(request.getNotificationId())
            .orElseThrow(() -> new RuntimeException("Notifica non trovata"));

        // Verifica che il device destinatario esista
        DeviceEntity device = deviceRepository.findByDeviceToken(request.getDeviceToken())
            .orElseThrow(() -> new RuntimeException("Device non trovato con token: " + request.getDeviceToken()));

        // Verifica che il device destinatario sia attivo (isActive = true in user_devices)
        List<UserDevice> activeAssociations = userDeviceRepository.findAllByDevice_Id(device.getId());
        boolean isDeviceActive = activeAssociations.stream()
                .anyMatch(ud -> Boolean.TRUE.equals(ud.getIsActive()));

        if (!isDeviceActive) {
            throw new RuntimeException("Il device con token " + request.getDeviceToken() + " non è attivo per nessun utente.");
        }

        // Verifica che il device richiedente esista
        if (deviceRepository.findByDeviceToken(request.getDeviceTokenRequest()).isEmpty()) {
            throw new RuntimeException("Device request non trovato con token: " + request.getDeviceTokenRequest());
        }

        DeviceNotification entity = deviceNotificationMapper.toEntity(request, notification);
        entity.setStatus(Status.PENDING);
        deviceNotificationRepository.save(entity);

        try {
            Message message = Message.builder()
                .setNotification(Notification.builder()
                    .setTitle(notification.getTitle())
                    .setBody(notification.getBody())
                    .build())
                .setToken(device.getDeviceToken())
                .putData("notificationId", String.valueOf(entity.getId()))
                .putData("deviceToken", String.valueOf(entity.getDeviceToken()))
                .putData("deviceTokenRequest", String.valueOf(entity.getDeviceTokenRequest()))
                .putData("type", String.valueOf(notification.getType()))
                .build();

            FirebaseMessaging.getInstance().send(message);
            entity.setStatus(Status.SENT);
            entity.setUpdatedAt(LocalDateTime.now());
            log.info("Notifica inviata con successo al device: {}", device.getDeviceToken());
        } catch (FirebaseMessagingException e) {
            entity.setStatus(Status.FAILED);
            entity.setErrorMessage(e.getMessage());
            log.error("Errore invio notifica Firebase: {}", e.getMessage());
        }

        deviceNotificationRepository.save(entity);
        return deviceNotificationMapper.toResponse(entity);
    }

    public DeviceNotificationResponse findById(Long id) {
        log.info("Finding device notification with id: {}", id);
        return deviceNotificationMapper.toResponse(deviceNotificationRepository.findById(id).orElseThrow());
    }

    public List<DeviceNotificationResponse> findAll() {
        log.info("Finding all device notifications");
        return deviceNotificationRepository.findAll()
                .stream()
                .map(deviceNotificationMapper::toResponse)
                .toList();
    }

    public void deleteById(Long id) {
        log.info("Deleting device notification with id: {}", id);
        deviceNotificationRepository.deleteById(id);
    }
    @Transactional
    public DeviceNotificationResponse updateDeviceNotification(Long id, com.r2u.notification.dto.request.DeviceNotificationUpdateRequest request) {
        log.info("Updating device notification with id: {}", id);
        DeviceNotification entity = deviceNotificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device notification not found"));
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        
        return deviceNotificationMapper.toResponse(deviceNotificationRepository.save(entity));
    }

    @Transactional
    public List<DeviceNotificationResponse> sendToAllActiveDevices(com.r2u.notification.dto.request.DeviceNotificationBroadcastRequest request) {
        log.info("Sending notification {} to all active devices requested by {}", request.getNotificationId(), request.getDeviceTokenRequest());

        if (!notificationRepository.existsById(request.getNotificationId())) {
            throw new RuntimeException("Notifica non trovata");
        }

        if (!deviceRepository.existsByDeviceToken(request.getDeviceTokenRequest())) {
            throw new RuntimeException("Device request non trovato con token: " + request.getDeviceTokenRequest());
        }

        List<UserDevice> activeUserDevices = userDeviceRepository.findByIsActiveTrue();
        
        return activeUserDevices.stream()
            .map(UserDevice::getDevice)
            .distinct()
            .map(device -> {
                DeviceNotificationRequest singleRequest = new DeviceNotificationRequest(
                    request.getNotificationId(),
                    device.getDeviceToken(),
                    request.getDeviceTokenRequest()
                );
                try {
                    return createDeviceNotification(singleRequest);
                } catch (Exception e) {
                    log.error("Errore invio notifica al device {}: {}", device.getDeviceToken(), e.getMessage());
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .toList();
    }
}
