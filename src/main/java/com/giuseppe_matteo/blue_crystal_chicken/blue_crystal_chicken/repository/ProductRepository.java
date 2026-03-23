package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;



@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByName(String name);
}
