package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents a product in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "PRODUCTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Category")
    private String category;

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

    // Bidirectional with MenuProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<MenuProduct> menuProducts;

    // Bidirectional with OfferProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OfferProduct> offerProducts;

    // Many-to-many with Ingredient via ProductIngredient
    @ManyToMany
    @JoinTable(
        name = "Product_Ingredients",
        joinColumns = @JoinColumn(name = "Product_id"),
        inverseJoinColumns = @JoinColumn(name = "Ingredient_id")
    )
    private List<Ingredient> ingredients;

    // Bidirectional with OrderProduct
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;
}