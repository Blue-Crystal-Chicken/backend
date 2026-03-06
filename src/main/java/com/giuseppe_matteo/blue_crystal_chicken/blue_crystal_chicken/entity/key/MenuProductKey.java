package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Composite key for MenuProduct junction table.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuProductKey implements Serializable {

    @Column(name = "Menu_id")
    private Long menuId;

    @Column(name = "Product_id")
    private Long productId;
}