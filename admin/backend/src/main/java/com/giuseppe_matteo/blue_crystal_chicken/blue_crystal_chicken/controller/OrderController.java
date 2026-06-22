package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OrderMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UpdateOrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderItemResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final NotificationPublisher notificationPublisher;

    // Id sede di un ordine (per indirizzare le notifiche al Manager della sede)
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
        if (token == null || token.isEmpty() || o.getLocation() == null)
            return false;
        String expected = o.getLocation().getStationToken();
        return expected != null && expected.equals(token);
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(
                orderService.findAll().stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findById(id)));
    }

    // GET /api/orders/code/{orderId}
    @GetMapping("/code/{orderId}")
    public ResponseEntity<OrderResponse> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(orderMapper.toResponse(orderService.findByOrderId(orderId)));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                orderService.findByUserId(userId).stream()
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

    // GET /api/orders/location/{locationId}
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<OrderResponse>> getByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(
                orderService.findByLocation(locationId).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/location/{locationId}/date-range?from=...&to=...
    @GetMapping("/location/{locationId}/date-range")
    public ResponseEntity<List<OrderResponse>> getByLocationAndDateRange(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                orderService.findByLocationAndDateRange(locationId, from, to).stream()
                        .map(orderMapper::toResponse)
                        .collect(Collectors.toList()));
    }

    // GET /api/orders/location/{locationId}/revenue?from=...&to=...
    @GetMapping("/location/{locationId}/revenue")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueByLocation(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        BigDecimal total = orderService.getRevenueByLocation(locationId, from, to);
        return ResponseEntity.ok(Map.of("total", total));
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

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest order) {
        OrderEntity created = orderService.create(order);
        // Canale stazioni → Manager di sede: nuovo ordine (totem/cassa)
        notificationPublisher.publish("event.order.created", "ORDER_CREATED", "INFO", "Ordini",
                "Nuovo ordine " + orderCode(created),
                "Ordine ricevuto (" + (created.getServiceType() != null ? created.getServiceType() : "—") + ")",
                locId(created));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(created));
    }

    // PUT /api/orders/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        OrderEntity updated = orderService.update(id, request);
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // PUT /api/orders/{id}/status?status=PREPARING
    // Autorizzato a: ADMIN (via JWT) OPPURE Cucina con il token-stazione (header
    // X-Station-Token) corrispondente alla SEDE dell'ordine.
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status,
            @RequestHeader(value = "X-Station-Token", required = false) String stationToken) {
        OrderEntity order = orderService.findById(id);
        if (!isAdmin() && !stationMatches(order, stationToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Non autorizzato: serve un token-stazione valido per la sede dell'ordine.");
        }
        OrderEntity updated = orderService.updateStatus(id, status);
        // Canale cucina → Manager/Tabellone: cambio stato (es. READY, CANCELLED)
        String level = "CANCELLED".equalsIgnoreCase(status) ? "WARNING"
                : "READY".equalsIgnoreCase(status) ? "SUCCESS" : "INFO";
        notificationPublisher.publish("event.order.status", "ORDER_STATUS", level, "Cucina",
                "Ordine " + orderCode(updated) + " → " + status,
                "Stato aggiornato a " + status,
                locId(updated));
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
