package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Junction entity for Order and Product with additional attributes.
 */
@Entity
@Table(name = "Order_Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "order", "product", "offer" })
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Additions")
    private BigDecimal additions;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Price")
    private BigDecimal price;

    @Column(name = "Special_note")
    private String specialNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "Order_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "Product_id", nullable = true)
    private ProductEntity product;

    @ManyToOne
    @JoinColumn(name = "Offer_id", nullable = true)
    private OfferEntity offer;

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
