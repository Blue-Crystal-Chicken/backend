package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.IngredientMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<IngredientResponse> findAll() {
        return ingredientRepository.findAll().stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }

    public IngredientResponse findResponseById(Long id) {
        return ingredientMapper.toResponse(findById(id));
    }

    public IngredientEntity findById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente non trovato con id: " + id));
    }

    public List<IngredientResponse> findByName(String name) {
        return ingredientRepository.findByNameContainingIgnoreCase(name).stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<IngredientResponse> findLowStock(Double threshold) {
        return ingredientRepository.findByQuantityLessThan(threshold).stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<IngredientResponse> findByProductId(Long productId) {
        return ingredientRepository.findByProductId(productId).stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<IngredientResponse> findByLocationId(Long locationId) {
        return ingredientRepository.findByLocationId(locationId).stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        if (ingredientRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ingrediente con nome '" + request.getName() + "' già esistente");
        }
        IngredientEntity ingredient = ingredientMapper.toEntity(request);
        return ingredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientResponse update(Long id, IngredientRequest request) {
        IngredientEntity existing = findById(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setQuantity(request.getQuantity());
        return ingredientMapper.toResponse(ingredientRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente non trovato con id: " + id);
        }
        ingredientRepository.deleteById(id);
    }
}
