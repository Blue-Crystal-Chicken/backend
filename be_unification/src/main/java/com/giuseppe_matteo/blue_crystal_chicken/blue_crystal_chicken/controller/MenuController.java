package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserFavoriteMenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.MenuService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;
    private final MenuMapper menuMapper;

    // GET /api/menus
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAll() {
        log.info("REST request to get all menus");
        return ResponseEntity.ok(menuService.findAll());
    }

    @GetMapping("/v1/top")
    public ResponseEntity<List<MenuResponse>> getAllPageable(@RequestParam(defaultValue = "5") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ResponseEntity.ok(menuService.findAll(pageable));
    }

    // GET /api/menus/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> getById(@PathVariable Long id) {
        log.info("REST request to get menu by id: {}", id);
        return ResponseEntity.ok(menuService.findMenuResponseById(id));
    }

    @GetMapping("/v1/menus/{id}/{userId}")
    public ResponseEntity<MenuResponse> getByIdWithUser(@PathVariable Long id, @PathVariable Long userId) {
        log.info("REST request to get menu by id: {} for user: {}", id, userId);
        return ResponseEntity.ok(menuService.findMenuResponseById(id, userId));
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
    public ResponseEntity<List<MenuResponse>> getMenuByProductId(@PathVariable Long productId) {
        log.debug("Ricevuta richiesta per menu con productId: {}", productId);
        return ResponseEntity.ok(menuService.findMenuByProductId(productId));
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
        log.info("REST request to create menu: {}", menu.getName());
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
        log.info("REST request to delete menu: {}", id);
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // FAVORITES

    @PostMapping("/favorite/v1/user/")
    public ResponseEntity<?> addUserFavoriteMenu(@RequestBody UserFavoriteMenuRequest request) {
        log.info("POST /api/menus/favorite/v1/user/{}", request.getUserId());
        try {
            return ResponseEntity.ok(menuService.addUserFavoriteMenu(request.getUserId(), request.getMenuId()));
        } catch (Exception e) {
            log.error("POST /api/menus/favorite/v1/user/{} - ERROR: {}", request.getUserId(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/favorite/v1/user/{id}")
    public ResponseEntity<?> getUserFavoriteMenus(@PathVariable Long id) {
        log.info("GET /api/menus/favorite/v1/user/{}", id);
        try {
            return ResponseEntity.ok(menuService.getUserFavoriteMenus(id));
        } catch (Exception e) {
            log.error("GET /api/menus/favorite/v1/user/{} - ERROR: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/favorite/v1/user/{userId}/{menuId}")
    public ResponseEntity<?> deleteUserFavoriteMenu(@PathVariable Long userId, @PathVariable Long menuId) {
        log.info("DELETE /api/menus/favorite/v1/user/{}/{}", userId, menuId);
        try {
            return ResponseEntity.ok(menuService.deleteUserFavoriteMenu(userId, menuId));
        } catch (Exception e) {
            log.error("DELETE /api/menus/favorite/v1/user/{}/{} - ERROR: {}", userId, menuId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
