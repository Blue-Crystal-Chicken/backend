package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;



@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsByName(String name);

    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByCategoryName(CategoryName name);

    @Query("SELECT op.product FROM OrderProduct op " +
       "GROUP BY op.product " +
       "ORDER BY COUNT(op.product.id) DESC")
    List<ProductEntity> findTop5MostOrderedProducts(Pageable pageable);
}
