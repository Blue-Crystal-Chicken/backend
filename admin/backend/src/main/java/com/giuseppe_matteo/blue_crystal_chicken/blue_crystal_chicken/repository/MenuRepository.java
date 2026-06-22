package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

    Optional<MenuEntity> findByName(String name);

    boolean existsByName(String name);

    List<MenuEntity> findByNameContainingIgnoreCase(String name);

    // Menu con prezzo entro un range
    List<MenuEntity> findByPriceBetween(Double minPrice, Double maxPrice);

    List<MenuEntity> findByPriceLessThanEqual(Double maxPrice);

    // Menu che contengono un determinato prodotto
    @Query("SELECT m FROM MenuEntity m JOIN m.menuProducts mp WHERE mp.product.id = :productId")
    List<MenuEntity> findByProductId(@Param("productId") Long productId);

    // Menu associati a un'offerta
    @Query("SELECT m FROM MenuEntity m JOIN m.offers o WHERE o.id = :offerId")
    List<MenuEntity> findByOfferId(@Param("offerId") Long offerId);
}
