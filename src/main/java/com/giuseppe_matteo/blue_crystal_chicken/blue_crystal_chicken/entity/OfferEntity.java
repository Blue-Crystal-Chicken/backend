package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an offer in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "OFFER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menus", "offerProducts" })
public class OfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome dell'offerta è obbligatorio")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Image")
    private String img_path;

    @Column(name = "Price")
    private Double price;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-many with Menu via OfferMenu
    @ManyToMany
    @JoinTable(name = "Offer_Menu", joinColumns = @JoinColumn(name = "Offer_id"), inverseJoinColumns = @JoinColumn(name = "Menu_id"))
    private List<MenuEntity> menus;

    // Bidirectional with OfferProduct
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;

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
