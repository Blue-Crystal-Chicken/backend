package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.LocationIngredientKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationIngredientRepository;
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
        return locationIngredientRepository.save(li);
    }

    @Transactional
    public void removeIngredientFromLocation(Long locationId, Long ingredientId) {
        if (!locationIngredientRepository.existsById_LocationIdAndId_IngredientId(locationId, ingredientId)) {
            throw new RuntimeException("Ingrediente non trovato in questa location");
        }
        locationIngredientRepository.deleteById_LocationIdAndId_IngredientId(locationId, ingredientId);
    }
}
