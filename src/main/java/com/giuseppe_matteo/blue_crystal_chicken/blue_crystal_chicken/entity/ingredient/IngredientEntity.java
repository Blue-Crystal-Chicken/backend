package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ingredient;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.product.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.LocationIngredient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

/**
 * Represents an ingredient in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "INGREDIENTS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "products", "locationIngredients" })
public class IngredientEntity extends AuditingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome deve essere compreso tra 3 e 50 caratteri")
    private String name;

    @Column(name = "Description")
    @Size(max = 255, message = "La descrizione non può superare i 255 caratteri")
    private String description;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Quantity")
    private Double quantity;

    // Many-to-many with Product via ProductIngredient
    @ManyToMany(mappedBy = "ingredients")
    private List<ProductEntity> products;

    // Bidirectional with LocationIngredient.
    // @JsonIgnore: rompe la ricorsione infinita Ingredient -> locationIngredients ->
    // LocationIngredient.ingredient -> ... che mandava in loop la serializzazione
    // (e quindi il backend) appena esisteva una riga di scorta.
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LocationIngredient> locationIngredients;
}
