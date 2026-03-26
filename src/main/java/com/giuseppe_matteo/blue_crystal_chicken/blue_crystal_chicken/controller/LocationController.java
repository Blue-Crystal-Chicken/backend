package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationIngredientService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationIngredientService locationIngredientService;

    // GET /api/locations
    @GetMapping
    public ResponseEntity<List<LocationEntity>> getAll() {
        return ResponseEntity.ok(locationService.findAll());
    }

    // GET /api/locations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<LocationEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    // GET /api/locations/city/{city}
    @GetMapping("/city/{city}")
    public ResponseEntity<List<LocationEntity>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(locationService.findByCity(city));
    }

    // GET /api/locations/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LocationEntity>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(locationService.findByStatus(status));
    }

    // POST /api/locations
    @PostMapping
    public ResponseEntity<LocationEntity> create(@RequestBody LocationEntity location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(location));
    }

    // PUT /api/locations/{id}
    @PutMapping("/{id}")
    public ResponseEntity<LocationEntity> update(@PathVariable Long id, @RequestBody LocationEntity location) {
        return ResponseEntity.ok(locationService.update(id, location));
    }

    // DELETE /api/locations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── SCORTE ──────────────────────────────────────────────────────────────

    // GET /api/locations/{id}/stock
    @GetMapping("/{id}/stock")
    public ResponseEntity<List<LocationIngredient>> getStock(@PathVariable Long id) {
        return ResponseEntity.ok(locationIngredientService.findByLocationId(id));
    }

    // GET /api/locations/{id}/stock/low?threshold=5
    @GetMapping("/{id}/stock/low")
    public ResponseEntity<List<LocationIngredient>> getLowStock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") Double threshold) {
        return ResponseEntity.ok(locationIngredientService.findLowStockByLocation(id, threshold));
    }

    // POST /api/locations/{locationId}/stock/{ingredientId}
    // Body: { "quantity": 50.0 }
    @PostMapping("/{locationId}/stock/{ingredientId}")
    public ResponseEntity<LocationIngredient> addStock(
            @PathVariable Long locationId,
            @PathVariable Long ingredientId,
            @RequestBody Map<String, Object> body) {
        Double quantity = ((Number) body.get("quantity")).doubleValue();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationIngredientService.addIngredientToLocation(locationId, ingredientId, quantity));
    }

    // PUT /api/locations/{locationId}/stock/{ingredientId}
    // Body: { "quantity": 30.0 }
    @PutMapping("/{locationId}/stock/{ingredientId}")
    public ResponseEntity<LocationIngredient> updateStock(
            @PathVariable Long locationId,
            @PathVariable Long ingredientId,
            @RequestBody Map<String, Object> body) {
        Double quantity = ((Number) body.get("quantity")).doubleValue();
        return ResponseEntity.ok(locationIngredientService.updateQuantity(locationId, ingredientId, quantity));
    }

    // DELETE /api/locations/{locationId}/stock/{ingredientId}
    @DeleteMapping("/{locationId}/stock/{ingredientId}")
    public ResponseEntity<Void> removeStock(
            @PathVariable Long locationId,
            @PathVariable Long ingredientId) {
        locationIngredientService.removeIngredientFromLocation(locationId, ingredientId);
        return ResponseEntity.noContent().build();
    }
}
