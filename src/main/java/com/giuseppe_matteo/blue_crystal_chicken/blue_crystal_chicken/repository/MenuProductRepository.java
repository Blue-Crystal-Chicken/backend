package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.MenuProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuProductRepository extends JpaRepository<MenuProduct, MenuProductKey> {

    List<MenuProduct> findById_MenuId(Long menuId);

    List<MenuProduct> findByProductId(Long productId);

    // Solo i prodotti obbligatori di un menu
    List<MenuProduct> findById_MenuIdAndObligatoryTrue(Long menuId);

    boolean existsById_MenuIdAndId_ProductId(Long menuId, Long productId);

    void deleteById_MenuIdAndId_ProductId(Long menuId, Long productId);
}