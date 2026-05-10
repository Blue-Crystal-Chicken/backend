package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.LocationResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationIngredientService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<LocationResponse>> getAll() {
        return ResponseEntity.ok(locationService.findAll());
    }

    // GET /api/locations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findLocationResponseById(id));
    }

    // GET /api/locations/city/{city}
    @GetMapping("/city/{city}")
    public ResponseEntity<List<LocationResponse>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(locationService.findByCity(city.substring(0, 1).toUpperCase() + city.substring(1)));
    }

    // GET /api/locations/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LocationResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(locationService.findByStatus(status));
    }

    // GET /api/locations/location/tables/{id}
    @GetMapping("/location/tables/{id}")
    public ResponseEntity<Integer> getTablesById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findTablesById(id));
    }

    // POST /api/locations
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> create(@RequestBody LocationRequest location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(location));
    }

    // PUT /api/locations/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> update(@PathVariable Long id, @RequestBody LocationRequest location) {
        return ResponseEntity.ok(locationService.update(id, location));
    }

    // DELETE /api/locations/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationIngredient> updateStock(
            @PathVariable Long locationId,
            @PathVariable Long ingredientId,
            @RequestBody Map<String, Object> body) {
        Double quantity = ((Number) body.get("quantity")).doubleValue();
        return ResponseEntity.ok(locationIngredientService.updateQuantity(locationId, ingredientId, quantity));
    }

    // DELETE /api/locations/{locationId}/stock/{ingredientId}
    @DeleteMapping("/{locationId}/stock/{ingredientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeStock(
            @PathVariable Long locationId,
            @PathVariable Long ingredientId) {
        locationIngredientService.removeIngredientFromLocation(locationId, ingredientId);
        return ResponseEntity.noContent().build();
    }


    // ── APERTURA/CHIUSURA ──────────────────────────────────────────────

    // GET /api/locations/status/open
    @GetMapping("/status/open")
    public ResponseEntity<List<LocationResponse>> getOpen() {
        return ResponseEntity.ok(locationService.findByIsOpen(true));
    }

    // GET /api/locations/status/closed
    @GetMapping("/status/closed")
    public ResponseEntity<List<LocationResponse>> getClosed() {
        return ResponseEntity.ok(locationService.findByIsOpen(false));
    }

    // PUT /api/locations/{id}/open
    @PutMapping("/{id}/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> open(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.setIsOpen(id, true));
    }

    // PUT /api/locations/{id}/close
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.setIsOpen(id, false));
    }

    // PUT /api/locations/status/open/all
    @PutMapping("/status/open/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationResponse>> openAll() {
        return ResponseEntity.ok(locationService.setAllIsOpen(true));
    }

    // PUT /api/locations/status/closed/all
    @PutMapping("/status/closed/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationResponse>> closeAll() {
        return ResponseEntity.ok(locationService.setAllIsOpen(false));
    }
}
