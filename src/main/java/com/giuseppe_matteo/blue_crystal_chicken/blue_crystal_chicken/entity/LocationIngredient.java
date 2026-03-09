package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.LocationIngredientKey;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Junction entity for Location and Ingredient with additional attributes.
 */
@Entity
@Table(name = "Location_Ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "location", "ingredient" })
public class LocationIngredient {

    @EmbeddedId
    private LocationIngredientKey id;

    @Column(name = "Quantity")
    private Double quantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "Location_id")
    private LocationPOJO location;

    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "Ingredient_id")
    private IngredientPOJO ingredient;

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
