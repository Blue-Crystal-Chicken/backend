package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(CategoryName name);
    
}