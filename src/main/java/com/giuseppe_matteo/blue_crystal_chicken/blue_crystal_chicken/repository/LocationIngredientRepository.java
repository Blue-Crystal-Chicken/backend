package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.LocationIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/*LocationIngredientRepository È il cuore della gestione scorte. 
sono tutte rilevanti per monitorare e aggiornare le scorte della sua location

Le query :

findById_LocationId, 
findLowStockByLocation 
existsById_*
 */

@Repository
public interface LocationIngredientRepository extends JpaRepository<LocationIngredient, LocationIngredientKey> {

    List<LocationIngredient> findById_LocationId(Long locationId);

    List<LocationIngredient> findById_IngredientId(Long ingredientId);

    // Stock sotto soglia in una specifica location
    @Query("SELECT li FROM LocationIngredient li WHERE li.location.id = :locationId AND li.quantity < :threshold")
    List<LocationIngredient> findLowStockByLocation(@Param("locationId") Long locationId,
            @Param("threshold") Double threshold);

    boolean existsById_LocationIdAndId_IngredientId(Long locationId, Long ingredientId);

    void deleteById_LocationIdAndId_IngredientId(Long locationId, Long ingredientId);

}