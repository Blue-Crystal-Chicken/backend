package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OfferProductKey;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Junction entity for Offer and Product with additional attributes.
 */
@Entity
@Table(name = "Offer_Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "offer", "product" })
public class OfferProduct {

    @EmbeddedId
    private OfferProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @MapsId("offerId")
    @JoinColumn(name = "Offer_id")
    private OfferEntity offer;

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
