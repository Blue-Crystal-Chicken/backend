package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import org.springframework.stereotype.Component;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.CategoryMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.CategoryResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;

@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName().name());
        return response;
    }

    @Override
    public Category toCategory(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(CategoryName.valueOf(categoryRequest.getName()));
        return category;
    }
    
}
