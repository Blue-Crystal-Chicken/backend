package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.CategoryService;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        createCategory();
    }

    private void createCategory() {
        for (CategoryName categoryName : CategoryName.values()) {
            categoryService.save(new CategoryRequest(categoryName.name()));
        }
    }

}
