package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // GET /api/ingredients
    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAll() {
        return ResponseEntity.ok(ingredientService.findAll());
    }

    // GET /api/ingredients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.findResponseById(id));
    }

    // GET /api/ingredients/search?name=pollo
    @GetMapping("/search")
    public ResponseEntity<List<IngredientResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(ingredientService.findByName(name));
    }

    // GET /api/ingredients/low-stock?threshold=10
    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientResponse>> getLowStock(@RequestParam Double threshold) {
        return ResponseEntity.ok(ingredientService.findLowStock(threshold));
    }

    // GET /api/ingredients/by-location/{locationId}
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<IngredientResponse>> getByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(ingredientService.findByLocationId(locationId));
    }

    // GET /api/ingredients/by-product/{productId}
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<IngredientResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ingredientService.findByProductId(productId));
    }

    // POST /api/ingredients
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientResponse> create(@RequestBody IngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.create(request));
    }

    // PUT /api/ingredients/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientResponse> update(@PathVariable Long id, @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(ingredientService.update(id, request));
    }

    // DELETE /api/ingredients/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
