package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final MenuMapper menuMapper;

    // GET /api/menus
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAll() {
        return ResponseEntity.ok(menuService.findAll());
    }

    // GET /api/menus/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findMenuResponseById(id));
    }

    // GET /api/menus/search?name=chicken
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(menuService.findByName(name));
    }

    // GET /api/menus/price?min=5&max=20
    @GetMapping("/price")
    public ResponseEntity<List<MenuResponse>> getByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(menuService.findByPriceRange(min, max));
    }

    // GET /api/menus/by-offer/{offerId}
    @GetMapping("/by-offer/{offerId}")
    public ResponseEntity<List<MenuResponse>> getByOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(menuService.findByOfferId(offerId));
    }

    // GET /api/menus/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<MenuProductResponse>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(menuMapper.toMenuProductResponseList(menuService.findProductsByMenuId(id)));
    }

    // GET /api/menus/product/{productId}
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<MenuProductResponse>> getMenuByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(menuMapper.toMenuProductResponseList(menuService.findMenuByProductId(productId)));
    }

    // GET /api/menus/{id}/products/obligatory
    @GetMapping("/{id}/products/obligatory")
    public ResponseEntity<List<MenuProductResponse>> getObligatoryProducts(@PathVariable Long id) {
        return ResponseEntity.ok(menuMapper.toMenuProductResponseList(menuService.findObligatoryProductsByMenuId(id)));
    }

    // POST /api/menus
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> create(@ModelAttribute MenuRequest menu) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(menu));
    }

    // PUT /api/menus/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuResponse> update(@PathVariable Long id, @ModelAttribute MenuRequest menu) {
        return ResponseEntity.ok(menuService.update(id, menu));
    }

    // POST /api/menus/{menuId}/products/{productId}
    // Body: { "quantity": 2, "obligatory": true }
    @PostMapping("/{menuId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuProductResponse> addProduct(
            @PathVariable Long menuId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        Integer quantity = (Integer) body.get("quantity");
        Boolean obligatory = (Boolean) body.get("obligatory");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuMapper.toMenuProductResponse(menuService.addProductToMenu(menuId, productId, quantity, obligatory)));
    }

    // DELETE /api/menus/{menuId}/products/{productId}
    @DeleteMapping("/{menuId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long menuId,
            @PathVariable Long productId) {
        menuService.removeProductFromMenu(menuId, productId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/menus/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
