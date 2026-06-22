package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OrderMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UpdateOrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderItemResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.order.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final NotificationPublisher notificationPublisher;

    // ── Helper sede/codice ordine + autorizzazione cambio stato ──────────────
    private static Long locId(OrderEntity o) {
        return o.getLocation() != null ? o.getLocation().getId() : null;
    }

    private static String orderCode(OrderEntity o) {
        return o.getCode() != null ? o.getCode() : (o.getOrderId() != null ? o.getOrderId() : "#" + o.getId());
    }

    // È un ADMIN autenticato via JWT?
    private static boolean isAdmin() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // Il token-stazione presentato corrisponde alla sede dell'ordine?
    private static boolean stationMatches(OrderEntity o, String token) {
        if (token == null || token.isEmpty() || o.getLocation() == null) return false;
        String expected = o.getLocation().getStationToken();
        return expected != null && expected.equals(token);
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        log.info("REST request to get all orders");
        return ResponseEntity.ok(
                orderService.findAll().stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        log.info("REST request to get order by id: {}", id);
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findById(id)));
    }

    // GET /api/orders/code/{orderId}
    @GetMapping("/code/{orderId}")
    public ResponseEntity<OrderResponse> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findByOrderId(orderId)));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                orderService.findByUserId(userId, pageable).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/service-type/{type}
    @GetMapping("/service-type/{type}")
    public ResponseEntity<List<OrderResponse>> getByServiceType(@PathVariable String type) {
        return ResponseEntity.ok(
                orderService.findByServiceType(type).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/date-range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                orderService.findByDateRange(from, to).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/revenue?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        BigDecimal total = orderService.getTotalRevenue(from, to);
        return ResponseEntity.ok(Map.of("total", total));
    }

    // GET /api/orders/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<OrderItemResponse>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.findProductsByOrderId(id).stream()
                        .map(orderMapper::toItemResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/product-count/{productId}
    @GetMapping("/product-count/{productId}")
    public ResponseEntity<Map<String, Integer>> getProductCount(@PathVariable Long productId) {
        Integer count = orderService.getProductOrderCount(productId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // GET /api/orders/location/{locationId}/preparing
    @GetMapping("/location/{locationId}/preparing")
    public ResponseEntity<List<OrderResponse>> getPreparingOrdersByLocation(@PathVariable Long locationId) {
        log.info("REST request to get orders in preparation for location id: {}", locationId);
        return ResponseEntity.ok(
                orderService.findOrdersInPreparationByLocationId(locationId).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/location/{locationId}/ready
    @GetMapping("/location/{locationId}/ready")
    public ResponseEntity<List<OrderResponse>> getReadyOrdersByLocation(@PathVariable Long locationId) {
        log.info("REST request to get orders ready for location id: {}", locationId);
        return ResponseEntity.ok(
                orderService.findOrdersReadyByLocationId(locationId).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest order) {
        log.info("REST request to create order: {}", order);
        OrderEntity created = orderService.create(order);
        // Canale stazioni → Manager di sede: nuovo ordine (da totem/cassa)
        notificationPublisher.publish("event.order.created", "ORDER_CREATED", "INFO", "Ordini",
                "Nuovo ordine " + orderCode(created), "Ordine creato in sede", locId(created));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(created));
    }

    // PUT /api/orders/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        log.info("REST request to update order {}: {}", id, request);
        OrderEntity updated = orderService.update(id, request);
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // PUT /api/orders/{id}/status?status=PREPARING
    // Autorizzato a: ADMIN (JWT) OPPURE Cucina/Tabellone/Cassa con il token-stazione
    // (header X-Station-Token) corrispondente alla SEDE dell'ordine.
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status,
            @RequestHeader(value = "X-Station-Token", required = false) String stationToken) {
        OrderEntity order = orderService.findById(id);
        if (!isAdmin() && !stationMatches(order, stationToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Non autorizzato: serve un token-stazione valido per la sede dell'ordine.");
        }
        OrderEntity updated = orderService.updateStatus(id, status);
        // Canale cucina → Manager/Tabellone: cambio stato
        String level = "CANCELLED".equalsIgnoreCase(status) ? "WARNING"
                : "READY".equalsIgnoreCase(status) ? "SUCCESS" : "INFO";
        notificationPublisher.publish("event.order.status", "ORDER_STATUS", level, "Cucina",
                "Ordine " + orderCode(updated) + " → " + status,
                "Stato aggiornato a " + status, locId(updated));
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete order: {}", id);
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
