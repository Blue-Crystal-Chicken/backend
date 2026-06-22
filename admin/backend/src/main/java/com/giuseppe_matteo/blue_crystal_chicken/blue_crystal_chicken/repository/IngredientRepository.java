package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
 
    Optional<IngredientEntity> findByName(String name);
 
    boolean existsByName(String name);
 
    List<IngredientEntity> findByNameContainingIgnoreCase(String name);
 
    // Ingredienti con quantità sotto una soglia (utile per scorte)
    List<IngredientEntity> findByQuantityLessThan(Double threshold);
 
    // Ingredienti usati in un determinato prodotto
    @Query("SELECT i FROM IngredientEntity i JOIN i.products p WHERE p.id = :productId")
    List<IngredientEntity> findByProductId(@Param("productId") Long productId);
 
    // Ingredienti disponibili in una determinata location
    @Query("SELECT i FROM IngredientEntity i JOIN i.locationIngredients li WHERE li.location.id = :locationId")
    List<IngredientEntity> findByLocationId(@Param("locationId") Long locationId);
}
