package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.MenuProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.product.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Menu and Product with additional attributes.
 */
@Entity
@Table(name = "Menu_Products")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menu", "product" })
public class MenuProduct extends AuditingField {

    @EmbeddedId
    private MenuProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Obligatory")
    private Boolean obligatory;

    @ManyToOne
    @MapsId("menuId")
    @JoinColumn(name = "Menu_id")
    private MenuEntity menu;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private ProductEntity product;
}
