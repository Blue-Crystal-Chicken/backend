package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderItemRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UpdateOrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.*;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.*;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final OfferRepository offerRepository;
    private final MenuRepository menuRepository;
    private final OrderMapper orderMapper;


    // ── READ ────────────────────────────────────────────────────────────────

    public List<OrderEntity> findAll() {
        return orderRepository.findAll();
    }

    public OrderEntity findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato con id: " + id));
    }

    public OrderEntity findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Ordine non trovato con orderId: " + orderId));
    }

    public List<OrderEntity> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<OrderEntity> findByServiceType(String serviceType) {
        return orderRepository.findByServiceType(serviceType);
    }

    public List<OrderEntity> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByCreatedAtBetween(from, to);
    }

    public List<OrderEntity> findByUserAndDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByUserIdAndCreatedAtBetween(userId, from, to);
    }

    public BigDecimal getTotalRevenue(LocalDateTime from, LocalDateTime to) {
        BigDecimal total = orderRepository.sumTotalBetween(from, to);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<OrderProduct> findProductsByOrderId(Long orderId) {
        return orderProductRepository.findByOrder_Id(orderId);
    }

    public Integer getProductOrderCount(Long productId) {
        Integer count = orderProductRepository.sumQuantityByProductId(productId);
        return count != null ? count : 0;
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public OrderEntity create(OrderRequest request) {
        // Map flat fields + generate orderId
        OrderEntity order = orderMapper.toEntity(request);

        if (orderRepository.existsByOrderId(order.getOrderId())) {
            throw new RuntimeException("Ordine con orderId '" + order.getOrderId() + "' già esistente");
        }

        // Lookup user
        if (request.getUserId() != null) {
            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User non trovato con id: " + request.getUserId()));
            order.setUser(user);
        }

        if (request.getLocationId() != null) {
            log.info("Cerco location per id: {}", request.getLocationId());
            LocationEntity location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location non trovata con id: " + request.getLocationId()));
            order.setLocation(location);
        } else if (request.getLocationName() != null) {
            log.info("Location non trovata con id, cerco per nome: {}", request.getLocationName());
            LocationEntity location = locationRepository
                    .findByNameAndAddressAndCity(request.getLocationName(), request.getLocationAddress(),
                            request.getLocationCity())
                    .orElseThrow(() -> new RuntimeException("Location non trovata con nome: " + request.getLocationName()));
            order.setLocation(location);
            log.info("Location trovata: " + location);
        }

        if (order.getLocation() == null) {
            log.error("Tentativo di creare un ordine senza una location valida. Request: {}", request);
            throw new RuntimeException("Impossibile creare l'ordine: location non trovata o non specificata.");
        }

        order.setCode(generateLastCode(order.getLocation().getId()));
        log.info("Codice generato: "+order.getCode());
        // Set initial status based on payment type
        if ("cash".equalsIgnoreCase(request.getPaymentType())) {
            order.setStatus(OrderStatus.PENDING);
        } else if ("card".equalsIgnoreCase(request.getPaymentType())) {
            order.setStatus(OrderStatus.PREPARING);
        } else {
            order.setStatus(OrderStatus.PENDING);
        }

        // Save order to get the ID
        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order creata: " + savedOrder);

        // Create OrderProducts with price snapshots
        BigDecimal total = BigDecimal.ZERO;
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OrderProduct> orderProducts = new ArrayList<>();
            for (OrderItemRequest item : request.getItems()) {
                OrderProduct op = new OrderProduct();
                op.setOrder(savedOrder);
                op.setQuantity(item.getQuantity() != null ? item.getQuantity() : 1);
                op.setSpecialNote(item.getSpecialNote());

                BigDecimal unitPrice = BigDecimal.ZERO;
                if (item.getOfferId() != null) {
                    OfferEntity offer = offerRepository.findById(item.getOfferId())
                            .orElseThrow(() -> new RuntimeException("Offerta non trovata con id: " + item.getOfferId()));
                    op.setOffer(offer);
                    log.info("Offerta trovata: " + offer);
                    unitPrice = BigDecimal.valueOf(offer.getPrice() != null ? offer.getPrice() : 0.0);
                } else if (item.getProductId() != null) {
                    ProductEntity product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("Prodotto non trovato con id: " + item.getProductId()));
                    op.setProduct(product);
                    unitPrice = BigDecimal.valueOf(product.getPrice() != null ? product.getPrice() : 0.0);
                } else if(item.getMenuId() != null) {
                    MenuEntity menu = menuRepository.findById(item.getMenuId())
                        .orElseThrow(() -> new RuntimeException("Menu non trovato con id: " + item.getMenuId()));
                    op.setMenu(menu);
                    unitPrice = BigDecimal.valueOf(menu.getPrice() != null ? menu.getPrice() : 0.0);
                }else {
                    throw new RuntimeException("Ogni elemento dell'ordine deve avere un productId, un offerId o un menuId");
                }
                // Aggiunta ingredienti se presenti
                if(item.getIngredientIds() != null){
                    ArrayList<IngredientEntity> ingredients= new ArrayList<>();
                    for(Long id : item.getIngredientIds()){
                        IngredientEntity ingredient = ingredientRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato con id: " + id));
                        ingredients.add(ingredient);
                        unitPrice = unitPrice.add(BigDecimal.valueOf(ingredient.getPrice() != null ? ingredient.getPrice() : 0.0));
                    }
                    op.setIngredients(ingredients);
                }
                op.setPrice(unitPrice);
                total = total.add(unitPrice.multiply(BigDecimal.valueOf(op.getQuantity())));
                orderProducts.add(op);
            }
            orderProductRepository.saveAll(orderProducts);
            savedOrder.setOrderProducts(orderProducts);
        }

        savedOrder.setTotalAt(total);
        savedOrder.setPaymentAmount(total);
        return orderRepository.save(savedOrder);
    }

    @Transactional
    public OrderEntity update(Long id, UpdateOrderRequest request) {
        OrderEntity existing = findById(id);
        if (request.getServiceType() != null) existing.setServiceType(request.getServiceType());
        if (request.getOrderType() != null) existing.setOrderType(request.getOrderType());
        if (request.getTableNumber() != null) existing.setTableNumber(request.getTableNumber());
        if (request.getPaymentType() != null) existing.setPaymentType(request.getPaymentType());
        if (request.getStatus() != null) existing.setStatus(OrderStatus.valueOf(request.getStatus()));
        return orderRepository.save(existing);
    }

    @Transactional
    public OrderEntity updateStatus(Long id, String status) {
        OrderEntity existing = findById(id);
        existing.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        return orderRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Ordine non trovato con id: " + id);
        }
        orderRepository.deleteById(id);
    }


    // UTILITY

    private String generateLastCode(Long location_id){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        String lastCode = orderRepository.findLastCodeByLocationIdAndCreatedAtBetween(location_id, startOfDay, endOfDay);
        if (lastCode == null) {
            return "001";
        }
        int lastCodeInt = Integer.parseInt(lastCode);
        return String.format("%03d", lastCodeInt + 1);
    }


    private BigDecimal totalAdditions(OrderItemRequest item){
        BigDecimal additions = BigDecimal.ZERO;
        return additions;
    }
}
