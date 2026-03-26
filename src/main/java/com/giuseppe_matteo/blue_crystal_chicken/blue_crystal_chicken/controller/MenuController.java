package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // GET /api/menus
    @GetMapping
    public ResponseEntity<List<MenuEntity>> getAll() {
        return ResponseEntity.ok(menuService.findAll());
    }

    // GET /api/menus/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MenuEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findById(id));
    }

    // GET /api/menus/search?name=chicken
    @GetMapping("/search")
    public ResponseEntity<List<MenuEntity>> search(@RequestParam String name) {
        return ResponseEntity.ok(menuService.findByName(name));
    }

    // GET /api/menus/price?min=5&max=20
    @GetMapping("/price")
    public ResponseEntity<List<MenuEntity>> getByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(menuService.findByPriceRange(min, max));
    }

    // GET /api/menus/by-offer/{offerId}
    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<List<MenuEntity>> getByOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(menuService.findByOfferId(offerId));
    }

    // GET /api/menus/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<MenuProduct>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findProductsByMenuId(id));
    }

    // GET /api/menus/{id}/products/obligatory
    @GetMapping("/{id}/products/obligatory")
    public ResponseEntity<List<MenuProduct>> getObligatoryProducts(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findObligatoryProductsByMenuId(id));
    }

    // POST /api/menus
    @PostMapping
    public ResponseEntity<MenuEntity> create(@RequestBody MenuEntity menu) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(menu));
    }

    // PUT /api/menus/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MenuEntity> update(@PathVariable Long id, @RequestBody MenuEntity menu) {
        return ResponseEntity.ok(menuService.update(id, menu));
    }

    // POST /api/menus/{menuId}/products/{productId}
    // Body: { "quantity": 2, "obligatory": true }
    @PostMapping("/{menuId}/products/{productId}")
    public ResponseEntity<MenuProduct> addProduct(
            @PathVariable Long menuId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        Integer quantity = (Integer) body.get("quantity");
        Boolean obligatory = (Boolean) body.get("obligatory");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.addProductToMenu(menuId, productId, quantity, obligatory));
    }

    // DELETE /api/menus/{menuId}/products/{productId}
    @DeleteMapping("/{menuId}/products/{productId}")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long menuId,
            @PathVariable Long productId) {
        menuService.removeProductFromMenu(menuId, productId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/menus/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
