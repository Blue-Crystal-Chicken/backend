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
    private Long id;

    // ==================== BASIC INFO ====================

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome deve essere compreso tra 3 e 50 caratteri")
    private String name;

    @Column(name = "Description")
    private String description;

    @JoinColumn(name = "category_id")
    @NotNull(message = "La categoria è obbligatoria")
    @ManyToOne
    private Category category;

    @Column(name = "Image")
    private String imgPath;

    @Column(name = "Available")
    private Boolean available = true;

    @Column(name = "Size")
    private String size;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "Weight")
    private Double weight;

    @Column(name = "Liters")
    private Double liters;

    @Column(name = "IsSpicy")
    private Boolean isSpicy;

    @Column(name = "Flavor")
    private String flavor;

    @Column(name = "Temperature")
    private String temperature;

    @Column(name = "IsCarbonated")
    private Boolean isCarbonated;

    @Column(name = "Calories")
    private Integer calories;

    @Column(name = "IsVegetarian")
    private Boolean isVegetarian;

    @Column(name = "IsVegan")
    private Boolean isVegan;

    @Column(name = "IsGlutenFree")
    private Boolean isGlutenFree;

    @Column(name = "Additions")
    private Double additions;

    @Column(name = "Price")
    private Double price;

    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;

    @ManyToMany
    @JoinTable(
        name = "Product_Ingredients",
        joinColumns = @JoinColumn(name = "Product_id"),
        inverseJoinColumns = @JoinColumn(name = "Ingredient_id")
    )
    private List<IngredientEntity> ingredients;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

    // ==================== TIMESTAMPS ====================

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

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