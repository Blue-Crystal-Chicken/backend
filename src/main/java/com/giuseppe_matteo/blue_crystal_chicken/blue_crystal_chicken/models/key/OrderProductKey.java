package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Composite key for OrderProduct junction table.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductKey implements Serializable {

    @Column(name = "Order_id")
    private Long orderId;

    @Column(name = "Product_id")
    private Long productId;
}