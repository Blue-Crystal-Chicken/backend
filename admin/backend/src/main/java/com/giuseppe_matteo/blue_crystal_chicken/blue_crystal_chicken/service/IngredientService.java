package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<IngredientEntity> findAll() {
        return ingredientRepository.findAll();
    }

    public IngredientEntity findById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato con id: " + id));
    }

    public List<IngredientEntity> findByName(String name) {
        return ingredientRepository.findByNameContainingIgnoreCase(name);
    }

    public List<IngredientEntity> findLowStock(Double threshold) {
        return ingredientRepository.findByQuantityLessThan(threshold);
    }

    public List<IngredientEntity> findByProductId(Long productId) {
        return ingredientRepository.findByProductId(productId);
    }

    public List<IngredientEntity> findByLocationId(Long locationId) {
        return ingredientRepository.findByLocationId(locationId);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public IngredientEntity create(IngredientEntity ingredient) {
        if (ingredientRepository.existsByName(ingredient.getName())) {
            throw new RuntimeException("Ingrediente con nome '" + ingredient.getName() + "' già esistente");
        }
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public IngredientEntity update(Long id, IngredientEntity updated) {
        IngredientEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setQuantity(updated.getQuantity());
        return ingredientRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente non trovato con id: " + id);
        }
        ingredientRepository.deleteById(id);
    }
}
