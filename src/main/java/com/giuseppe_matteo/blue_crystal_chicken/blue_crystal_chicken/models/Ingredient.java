package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents an ingredient in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "INGREDIENTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Quantity")
    private Double quantity;

    // Many-to-many with Product via ProductIngredient
    @ManyToMany(mappedBy = "ingredients")
    private List<Product> products;

    // Bidirectional with LocationIngredient
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<LocationIngredient> locationIngredients;
}