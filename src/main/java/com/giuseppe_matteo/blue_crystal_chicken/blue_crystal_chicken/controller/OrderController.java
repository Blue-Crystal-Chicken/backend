package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;
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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderEntity>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    // GET /api/orders/code/{orderId}
    @GetMapping("/code/{orderId}")
    public ResponseEntity<OrderEntity> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.findByOrderId(orderId));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderEntity>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    // GET /api/orders/service-type/{type}
    @GetMapping("/service-type/{type}")
    public ResponseEntity<List<OrderEntity>> getByServiceType(@PathVariable String type) {
        return ResponseEntity.ok(orderService.findByServiceType(type));
    }

    // GET /api/orders/date-range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @GetMapping("/date-range")
    public ResponseEntity<List<OrderEntity>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(orderService.findByDateRange(from, to));
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
    public ResponseEntity<List<OrderProduct>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findProductsByOrderId(id));
    }

    // GET /api/orders/product-count/{productId}
    @GetMapping("/product-count/{productId}")
    public ResponseEntity<Map<String, Integer>> getProductCount(@PathVariable Long productId) {
        Integer count = orderService.getProductOrderCount(productId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderEntity> create(@RequestBody OrderEntity order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(order));
    }

    // PUT /api/orders/{id}
    @PutMapping("/{id}")
    public ResponseEntity<OrderEntity> update(@PathVariable Long id, @RequestBody OrderEntity order) {
        return ResponseEntity.ok(orderService.update(id, order));
    }

    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
