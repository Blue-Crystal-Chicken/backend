package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.MenuProductKey;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Junction entity for Menu and Product with additional attributes.
 */
@Entity
@Table(name = "Menu_Products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menu", "product" })
public class MenuProduct {

    @EmbeddedId
    private MenuProductKey id;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Obligatory")
    private Boolean obligatory;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @MapsId("menuId")
    @JoinColumn(name = "Menu_id")
    private MenuEntity menu;

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
