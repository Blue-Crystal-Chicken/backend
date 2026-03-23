package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.ProductMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.IngredientResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ProductMapperImpl implements ProductMapper {

    private final ModelMapper modelMapper;

    public ProductMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductResponse toResponse(ProductEntity product) {
        if (product == null) return null;
        
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        
        if (product.getIngredients() != null) {
            response.setIngredients(product.getIngredients().stream()
                    .map(this::toIngredientResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setIngredients(Collections.emptyList());
        }
        
        return response;
    }

    @Override
    public ProductEntity toEntity(ProductRequest request) {
        if (request == null) return null;
        // Basic mapping for simple fields
        ProductEntity entity = modelMapper.map(request, ProductEntity.class);
        // Note: ingredients are linked in the Service layer using ingredientIds
        return entity;
    }

    private IngredientResponse toIngredientResponse(IngredientEntity ingredient) {
        if (ingredient == null) return null;
        return modelMapper.map(ingredient, IngredientResponse.class);
    }
}