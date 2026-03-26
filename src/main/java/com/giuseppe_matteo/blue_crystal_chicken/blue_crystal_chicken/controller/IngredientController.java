package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // GET /api/ingredients
    @GetMapping
    public ResponseEntity<List<IngredientEntity>> getAll() {
        return ResponseEntity.ok(ingredientService.findAll());
    }

    // GET /api/ingredients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<IngredientEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.findById(id));
    }

    // GET /api/ingredients/search?name=pollo
    @GetMapping("/search")
    public ResponseEntity<List<IngredientEntity>> search(@RequestParam String name) {
        return ResponseEntity.ok(ingredientService.findByName(name));
    }

    // GET /api/ingredients/low-stock?threshold=10
    @GetMapping("/low-stock")
    public ResponseEntity<List<IngredientEntity>> getLowStock(@RequestParam Double threshold) {
        return ResponseEntity.ok(ingredientService.findLowStock(threshold));
    }

    // GET /api/ingredients/by-location/{locationId}
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<IngredientEntity>> getByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(ingredientService.findByLocationId(locationId));
    }

    // GET /api/ingredients/by-product/{productId}
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<IngredientEntity>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ingredientService.findByProductId(productId));
    }

    // POST /api/ingredients
    @PostMapping
    public ResponseEntity<IngredientEntity> create(@RequestBody IngredientEntity ingredient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.create(ingredient));
    }

    // PUT /api/ingredients/{id}
    @PutMapping("/{id}")
    public ResponseEntity<IngredientEntity> update(@PathVariable Long id, @RequestBody IngredientEntity ingredient) {
        return ResponseEntity.ok(ingredientService.update(id, ingredient));
    }

    // DELETE /api/ingredients/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
