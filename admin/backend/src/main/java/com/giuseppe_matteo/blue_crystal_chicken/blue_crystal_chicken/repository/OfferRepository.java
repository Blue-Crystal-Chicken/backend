package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, Long> {

    Optional<OfferEntity> findByName(String name);

    boolean existsByName(String name);

    List<OfferEntity> findByNameContainingIgnoreCase(String name);

    // Offerte che contengono un determinato menu
    @Query("SELECT o FROM OfferEntity o JOIN o.menus m WHERE m.id = :menuId")
    List<OfferEntity> findByMenuId(@Param("menuId") Long menuId);

    // Offerte che contengono un determinato prodotto (via OfferProduct)
    @Query("SELECT o FROM OfferEntity o JOIN o.offerProducts op WHERE op.product.id = :productId")
    List<OfferEntity> findByProductId(@Param("productId") Long productId);
}