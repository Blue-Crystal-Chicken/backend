package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents an offer in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "OFFER")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    // Many-to-many with Menu via OfferMenu
    @ManyToMany
    @JoinTable(
        name = "Offer_Menu",
        joinColumns = @JoinColumn(name = "Offer_id"),
        inverseJoinColumns = @JoinColumn(name = "Menu_id")
    )
    private List<Menu> menus;

    // Bidirectional with OfferProduct
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;
}