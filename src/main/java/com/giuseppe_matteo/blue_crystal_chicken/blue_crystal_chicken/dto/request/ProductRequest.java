package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private Category category;
    private String size;
    private Integer quantity;
    private Double additions;
    private Double price;
    private String nutritionalInfo;
    private MultipartFile image;
    private String imagePath;
    private List<Long> ingredientIds;
}
