package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OrderProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

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

    // Totale incassato in un periodo — utile per le dashboard manager
    public BigDecimal getTotalRevenue(LocalDateTime from, LocalDateTime to) {
        BigDecimal total = orderRepository.sumTotalBetween(from, to);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<OrderProduct> findProductsByOrderId(Long orderId) {
        return orderProductRepository.findById_OrderId(orderId);
    }

    // Quante volte è stato ordinato un prodotto
    public Integer getProductOrderCount(Long productId) {
        Integer count = orderProductRepository.sumQuantityByProductId(productId);
        return count != null ? count : 0;
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public OrderEntity create(OrderEntity order) {
        if (orderRepository.existsByOrderId(order.getOrderId())) {
            throw new RuntimeException("Ordine con orderId '" + order.getOrderId() + "' già esistente");
        }
        return orderRepository.save(order);
    }

    @Transactional
    public OrderEntity update(Long id, OrderEntity updated) {
        OrderEntity existing = findById(id);
        existing.setServiceType(updated.getServiceType());
        existing.setOrderType(updated.getOrderType());
        existing.setTableNumber(updated.getTableNumber());
        existing.setPaymentType(updated.getPaymentType());
        existing.setPaymentAmount(updated.getPaymentAmount());
        existing.setTotalAt(updated.getTotalAt());
        return orderRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Ordine non trovato con id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
