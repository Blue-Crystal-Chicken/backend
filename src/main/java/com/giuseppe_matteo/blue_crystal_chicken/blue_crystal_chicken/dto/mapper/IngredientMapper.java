package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;

public interface IngredientMapper {

    IngredientResponse toResponse(IngredientEntity ingredient);
    IngredientEntity toEntity(IngredientRequest request);
}
