package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.LocationIngredientKey;

import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Location and Ingredient with additional attributes.
 */
@Entity
@Table(name = "Location_Ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationIngredient {

    @EmbeddedId
    private LocationIngredientKey id;

    @Column(name = "Quantity")
    private Double quantity;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "Location_id")
    private Location location;

    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "Ingredient_id")
    private Ingredient ingredient;
}