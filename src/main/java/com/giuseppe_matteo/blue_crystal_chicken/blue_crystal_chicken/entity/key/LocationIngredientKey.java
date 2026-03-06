package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * Composite key for LocationIngredient junction table.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationIngredientKey implements Serializable {

    @Column(name = "Location_id")
    private Long locationId;

    @Column(name = "Ingredient_id")
    private Long ingredientId;
}