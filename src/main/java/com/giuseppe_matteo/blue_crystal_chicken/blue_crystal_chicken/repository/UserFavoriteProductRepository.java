package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.UserFavoriteProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.UserProductKey;


@Repository
public interface UserFavoriteProductRepository extends JpaRepository<UserFavoriteProduct, UserProductKey> {

    Integer countByIdProductId(Long productId);

    List<UserFavoriteProduct> findByIdUserId(Long userId);

}