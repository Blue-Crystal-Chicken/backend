package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.MenuProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductService productService;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<MenuEntity> findAll() {
        return menuRepository.findAll();
    }

    public MenuEntity findById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu non trovato con id: " + id));
    }

    public List<MenuEntity> findByName(String name) {
        return menuRepository.findByNameContainingIgnoreCase(name);
    }

    public List<MenuEntity> findByPriceRange(Double min, Double max) {
        return menuRepository.findByPriceBetween(min, max);
    }

    public List<MenuEntity> findByOfferId(Long offerId) {
        return menuRepository.findByOfferId(offerId);
    }

    public List<MenuProduct> findProductsByMenuId(Long menuId) {
        return menuProductRepository.findById_MenuId(menuId);
    }

    public List<MenuProduct> findObligatoryProductsByMenuId(Long menuId) {
        return menuProductRepository.findById_MenuIdAndObligatoryTrue(menuId);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public MenuEntity create(MenuEntity menu) {
        if (menuRepository.existsByName(menu.getName())) {
            throw new RuntimeException("Menu con nome '" + menu.getName() + "' già esistente");
        }
        return menuRepository.save(menu);
    }

    @Transactional
    public MenuEntity update(Long id, MenuEntity updated) {
        MenuEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());
        return menuRepository.save(existing);
    }

    @Transactional
    public MenuProduct addProductToMenu(Long menuId, Long productId, Integer quantity, Boolean obligatory) {
        if (menuProductRepository.existsById_MenuIdAndId_ProductId(menuId, productId)) {
            throw new RuntimeException("Prodotto già presente nel menu");
        }
        MenuEntity menu = findById(menuId);
        ProductEntity product = productService.findById(productId);

        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setId(new MenuProductKey(menuId, productId));
        menuProduct.setMenu(menu);
        menuProduct.setProduct(product);
        menuProduct.setQuantity(quantity);
        menuProduct.setObligatory(obligatory);
        return menuProductRepository.save(menuProduct);
    }

    @Transactional
    public void removeProductFromMenu(Long menuId, Long productId) {
        if (!menuProductRepository.existsById_MenuIdAndId_ProductId(menuId, productId)) {
            throw new RuntimeException("Prodotto non trovato nel menu");
        }
        menuProductRepository.deleteById_MenuIdAndId_ProductId(menuId, productId);
    }

    @Transactional
    public void delete(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new RuntimeException("Menu non trovato con id: " + id);
        }
        menuRepository.deleteById(id);
    }
}
