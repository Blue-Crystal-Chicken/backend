package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OrderProductKey;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Junction entity for Order and Product with additional attributes.
 */
@Entity
@Table(name = "Order_Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "order", "product" })
public class OrderProduct {

    @EmbeddedId
    private OrderProductKey id;

    @Column(name = "Additions")
    private Double additions;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Special_note")
    private String specialNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "Order_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private OrderEntity order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private ProductEntity product;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
