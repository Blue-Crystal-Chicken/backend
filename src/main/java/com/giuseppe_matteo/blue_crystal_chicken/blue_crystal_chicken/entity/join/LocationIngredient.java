package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.LocationIngredientKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ingredient.IngredientEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Junction entity for Location and Ingredient with additional attributes.
 */
@Entity
@Table(name = "Location_Ingredients")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "location", "ingredient" })
public class LocationIngredient extends AuditingField {

    @EmbeddedId
    private LocationIngredientKey id;

    @Column(name = "Quantity")
    private Double quantity;

    @ManyToOne
    @MapsId("locationId")
    @JoinColumn(name = "Location_id")
    private LocationEntity location;

    @ManyToOne
    @MapsId("ingredientId")
    @JoinColumn(name = "Ingredient_id")
    private IngredientEntity ingredient;
}
