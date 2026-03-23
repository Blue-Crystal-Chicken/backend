package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {
}
