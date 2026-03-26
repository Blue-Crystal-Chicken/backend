package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a product in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "PRODUCTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "menuProducts", "offerProducts", "ingredients", "orderProducts" })
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome deve essere compreso tra 3 e 50 caratteri")
    private String name;

    @JoinColumn(name = "category_id")
    @NotNull(message = "La categoria è obbligatoria")
    @ManyToOne
    private Category category;

    @Column(name = "Size")
    private String size;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Additions")
    private Double additions;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Nutritional_info")
    private String nutritionalInfo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Bidirectional with MenuProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;

    // Bidirectional with OfferProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;

    // Many-to-many with Ingredient via ProductIngredient
    @ManyToMany
    @JoinTable(name = "Product_Ingredients", joinColumns = @JoinColumn(name = "Product_id"), inverseJoinColumns = @JoinColumn(name = "Ingredient_id"))
    private List<IngredientEntity> ingredients;

    // Bidirectional with OrderProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

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
