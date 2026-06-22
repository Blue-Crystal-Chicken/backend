package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.offer;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.OfferProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

/**
 * Represents an offer in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "OFFER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menus", "offerProducts" })
public class OfferEntity extends AuditingField {

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
    private String imgPath;

    @Column(name = "Price")
    private Double price;

    // Many-to-many with Menu via OfferMenu
    @ManyToMany
    @JoinTable(name = "Offer_Menu", joinColumns = @JoinColumn(name = "Offer_id"), inverseJoinColumns = @JoinColumn(name = "Menu_id"))
    private List<MenuEntity> menus;

    // Bidirectional with OfferProduct
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;
}
