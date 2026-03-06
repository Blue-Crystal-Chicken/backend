package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Composite key for OfferProduct junction table.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferProductKey implements Serializable {

    @Column(name = "Offer_id")
    private Long offerId;

    @Column(name = "Product_id")
    private Long productId;
}