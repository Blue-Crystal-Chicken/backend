package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.projection.CategoryWithCount;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(CategoryName name);

    boolean existsByName(CategoryName name);

    // Query che restituisce la categorie con il numero totale di prodotti presenti per quella determinata categorie
    @Query("""
        SELECT c AS category,
        (SELECT COUNT(p) FROM ProductEntity p WHERE p.category = c) AS count
        FROM Category c
    """)
    List<CategoryWithCount> findAllCategoriesWithProductCount();

}