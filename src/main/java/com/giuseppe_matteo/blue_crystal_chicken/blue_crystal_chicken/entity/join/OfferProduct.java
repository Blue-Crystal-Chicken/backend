package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.OfferProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.offer.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.product.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Offer and Product with additional attributes.
 */
@Entity
@Table(name = "Offer_Products")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "offer", "product" })
public class OfferProduct extends AuditingField {

    @EmbeddedId
    private OfferProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @ManyToOne
    @MapsId("offerId")
    @JoinColumn(name = "Offer_id")
    private OfferEntity offer;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private ProductEntity product;
}
