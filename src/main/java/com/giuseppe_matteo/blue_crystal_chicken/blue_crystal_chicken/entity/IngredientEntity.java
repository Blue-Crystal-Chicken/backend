package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an ingredient in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "INGREDIENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "products", "locationIngredients" })
public class IngredientEntity {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-many with Product via ProductIngredient
    @ManyToMany(mappedBy = "ingredients")
    private List<ProductEntity> products;

    // Bidirectional with LocationIngredient
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<LocationIngredient> locationIngredients;

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
