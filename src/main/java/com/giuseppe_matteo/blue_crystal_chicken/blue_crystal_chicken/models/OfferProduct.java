package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models.key.OfferProductKey;
import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Offer and Product with additional attributes.
 */
@Entity
@Table(name = "Offer_Products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferProduct {

    @EmbeddedId
    private OfferProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @ManyToOne
    @MapsId("offerId")
    @JoinColumn(name = "Offer_id")
    private Offer offer;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private Product product;
}