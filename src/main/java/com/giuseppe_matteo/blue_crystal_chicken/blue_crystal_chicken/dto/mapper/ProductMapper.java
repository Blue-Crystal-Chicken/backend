package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;

public interface ProductMapper {

    ProductResponse toResponse(ProductEntity product);
    ProductEntity toEntity(ProductRequest request);
}