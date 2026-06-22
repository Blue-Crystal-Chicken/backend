package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.LocationIngredientKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationIngredientRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationIngredientService {

    private final LocationIngredientRepository locationIngredientRepository;
    private final LocationService locationService;
    private final IngredientService ingredientService;
    private final NotificationPublisher notificationPublisher;

    // Soglia oltre la quale una giacenza di sede genera una notifica-log
    private static final double LOW_STOCK_THRESHOLD = 10.0;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<LocationIngredient> findByLocationId(Long locationId) {
        return locationIngredientRepository.findById_LocationId(locationId);
    }

    public List<LocationIngredient> findByIngredientId(Long ingredientId) {
        return locationIngredientRepository.findById_IngredientId(ingredientId);
    }

    public List<LocationIngredient> findLowStockByLocation(Long locationId, Double threshold) {
        return locationIngredientRepository.findLowStockByLocation(locationId, threshold);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public LocationIngredient addIngredientToLocation(Long locationId, Long ingredientId, Double quantity) {
        if (locationIngredientRepository.existsById_LocationIdAndId_IngredientId(locationId, ingredientId)) {
            throw new RuntimeException("Ingrediente già associato a questa location");
        }
        LocationEntity location = locationService.findById(locationId);
        IngredientEntity ingredient = ingredientService.findById(ingredientId);

        LocationIngredient li = new LocationIngredient();
        li.setId(new LocationIngredientKey(locationId, ingredientId));
        li.setLocation(location);
        li.setIngredient(ingredient);
        li.setQuantity(quantity);
        return locationIngredientRepository.save(li);
    }

    @Transactional
    public LocationIngredient updateQuantity(Long locationId, Long ingredientId, Double newQuantity) {
        LocationIngredientKey key = new LocationIngredientKey(locationId, ingredientId);
        LocationIngredient li = locationIngredientRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Scorta non trovata per questa location e ingrediente"));
        li.setQuantity(newQuantity);
        LocationIngredient saved = locationIngredientRepository.save(li);
        maybeNotifyLowStock(saved);
        return saved;
    }

    // Pubblica una notifica-log se la giacenza scende sotto la soglia critica
    private void maybeNotifyLowStock(LocationIngredient li) {
        if (li.getQuantity() != null && li.getQuantity() < LOW_STOCK_THRESHOLD) {
            String city = li.getLocation() != null ? li.getLocation().getCity() : "sede";
            String ingredient = li.getIngredient() != null ? li.getIngredient().getName() : "ingrediente";
            notificationPublisher.publish("event.stock.low", "LOW_STOCK", "WARNING", "Magazzino",
                    "Scorta sotto soglia",
                    "\"" + ingredient + "\" a " + city + " è sceso a " + li.getQuantity() + " (soglia " + LOW_STOCK_THRESHOLD + ").");
        }
    }

    @Transactional
    public void removeIngredientFromLocation(Long locationId, Long ingredientId) {
        if (!locationIngredientRepository.existsById_LocationIdAndId_IngredientId(locationId, ingredientId)) {
            throw new RuntimeException("Ingrediente non trovato in questa location");
        }
        locationIngredientRepository.deleteById_LocationIdAndId_IngredientId(locationId, ingredientId);
    }
}
