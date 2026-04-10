package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OrderMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UpdateOrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderItemResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(created));
    }

    // PUT /api/orders/{id}
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        OrderEntity updated = orderService.update(id, request);
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // PUT /api/orders/{id}/status?status=PREPARING
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        OrderEntity updated = orderService.updateStatus(id, status);
        return ResponseEntity.ok(orderMapper.toResponse(updated));
    }

    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
