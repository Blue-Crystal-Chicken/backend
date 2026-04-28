package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.IngredientMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapperImpl implements IngredientMapper {

    @Override
    public IngredientResponse toResponse(IngredientEntity ingredient) {
        if (ingredient == null) return null;
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setPrice(ingredient.getPrice());
        response.setQuantity(ingredient.getQuantity());
        return response;
    }

    @Override
    public IngredientEntity toEntity(IngredientRequest request) {
        if (request == null) return null;
        IngredientEntity entity = new IngredientEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setQuantity(request.getQuantity());
        return entity;
    }
}
