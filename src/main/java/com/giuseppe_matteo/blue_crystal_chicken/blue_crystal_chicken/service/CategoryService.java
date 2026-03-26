package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.CategoryResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.CategoryMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;

@Service
@Transactional
@Slf4j
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    public ResponseEntity<?> save(CategoryRequest request) {
        if (categoryRepository.existsByName(CategoryName.valueOf(request.getName()))) {
            log.info("Category {} already exists, skipping...", request.getName());
            return ResponseEntity.ok("Category already exists");
        }

        log.info("Creating category: {}", request);
        try {
            Category category = categoryMapper.toCategory(request);
            return ResponseEntity.ok(categoryRepository.save(category));
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public List<CategoryResponse> findAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::toCategoryResponse).collect(Collectors.toList());
    }

}