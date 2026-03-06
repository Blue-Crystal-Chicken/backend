package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.MenuProductKey;

import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Menu and Product with additional attributes.
 */
@Entity
@Table(name = "Menu_Products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuProduct {

    @EmbeddedId
    private MenuProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Obligatory")
    private Boolean obligatory;

    @ManyToOne
    @MapsId("menuId")
    @JoinColumn(name = "Menu_id")
    private MenuPOJO menu;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "Product_id")
    private ProductPOJO product;
}