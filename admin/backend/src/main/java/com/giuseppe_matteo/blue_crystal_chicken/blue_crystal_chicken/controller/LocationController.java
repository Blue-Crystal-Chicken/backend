package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationCreateRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationIngredientService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationIngredientService locationIngredientService;

    @Autowired
    private NotificationPublisher notificationPublisher;

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
        return ResponseEntity.ok(locationService.findByCity(city.substring(0, 1).toUpperCase() + city.substring(1)));
    }

    // GET /api/locations/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LocationEntity>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(locationService.findByStatus(status));
    }

    // POST /api/locations
    // Crea la sede e, se nel body sono presenti managerEmail+managerPassword,
    // crea anche l'account MANAGER collegato alla sede.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationEntity> create(@RequestBody LocationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createWithManager(request));
    }

    // GET /api/locations/{id}/station-token  → token-stazione per la Cucina (solo Admin)
    @GetMapping("/{id}/station-token")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getStationToken(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("stationToken", locationService.getStationToken(id)));
    }

    // GET /api/locations/by-station-token  → la stazione (Cucina/Tabellone) scopre
    // a quale sede appartiene il proprio token (header X-Station-Token). Pubblico:
    // è il token stesso a fare da credenziale.
    @GetMapping("/by-station-token")
    public ResponseEntity<?> byStationToken(
            @RequestHeader(value = "X-Station-Token", required = false) String token) {
        LocationEntity loc = locationService.findByStationToken(token);
        if (loc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Token-stazione mancante o non valido"));
        }
        return ResponseEntity.ok(Map.of(
                "id", loc.getId(),
                "name", loc.getName() != null ? loc.getName() : "",
                "city", loc.getCity() != null ? loc.getCity() : ""));
    }

    // PUT /api/locations/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationEntity> update(@PathVariable Long id, @RequestBody LocationEntity location) {
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
    public ResponseEntity<List<LocationEntity>> getOpen() {
        return ResponseEntity.ok(locationService.findByIsOpen(true));
    }

    // GET /api/locations/status/closed
    @GetMapping("/status/closed")
    public ResponseEntity<List<LocationEntity>> getClosed() {
        return ResponseEntity.ok(locationService.findByIsOpen(false));
    }

    // PUT /api/locations/{id}/open
    @PutMapping("/{id}/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationEntity> open(@PathVariable Long id) {
        LocationEntity loc = locationService.setIsOpen(id, true);
        notificationPublisher.publish("event.location.opened", "LOCATION_OPENED", "SUCCESS", "Sedi",
                "Sede aperta", "La sede di " + loc.getCity() + " è stata aperta.", loc.getId());
        return ResponseEntity.ok(loc);
    }

    // PUT /api/locations/{id}/close
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationEntity> close(@PathVariable Long id) {
        LocationEntity loc = locationService.setIsOpen(id, false);
        notificationPublisher.publish("event.location.closed", "LOCATION_CLOSED", "WARNING", "Sedi",
                "Sede chiusa", "La sede di " + loc.getCity() + " è stata chiusa.", loc.getId());
        return ResponseEntity.ok(loc);
    }

    // PUT /api/locations/status/open/all
    @PutMapping("/status/open/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> openAll() {
        locationService.setAllIsOpen(true);
        return ResponseEntity.ok().build();
    }

    // PUT /api/locations/status/closed/all
    @PutMapping("/status/closed/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeAll() {
        locationService.setAllIsOpen(false);
        return ResponseEntity.ok().build();
    }
}
