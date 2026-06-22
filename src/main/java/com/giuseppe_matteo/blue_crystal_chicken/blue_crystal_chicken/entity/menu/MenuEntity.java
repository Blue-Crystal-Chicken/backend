package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.offer.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.MenuProduct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

/**
 * Represents a menu item in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "MENU")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "offers", "menuProducts" })
public class MenuEntity extends AuditingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Description")
    private String description;

    @Column(name = "Image")
    private String imgPath;

    // Many-to-many with Offer via OfferMenu
    @ManyToMany(mappedBy = "menus")
    private List<OfferEntity> offers;

    // Bidirectional with MenuProduct
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;
}
