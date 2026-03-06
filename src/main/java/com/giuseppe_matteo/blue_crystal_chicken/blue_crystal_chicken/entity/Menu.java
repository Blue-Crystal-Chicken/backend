package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents a menu item in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "MENU")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Description")
    private String description;

    // Many-to-many with Offer via OfferMenu
    @ManyToMany(mappedBy = "menus")
    private List<Offer> offers;

    // Bidirectional with MenuProduct
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;
}