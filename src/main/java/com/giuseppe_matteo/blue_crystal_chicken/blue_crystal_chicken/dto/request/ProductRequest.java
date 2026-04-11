package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ProductRequest {

    // ------------------------
    // BASIC INFO
    // ------------------------
    private String name;
    private String categoryName;
    private String description;


    // ------------------------
    // GENERIC ATTRIBUTES
    // ------------------------
    private String size;
    private Integer quantity;
    private Double weight;
    private Double liters;

    // ------------------------
    // FOOD PROPERTIES
    // ------------------------
    private Boolean isSpicy;
    private String flavor;
    private String temperature;
    private Boolean isCarbonated;

    // ------------------------
    // NUTRITION
    // ------------------------
    private Integer calories;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;

    // ------------------------
    // PRICE
    // ------------------------
    private Double additions;
    private Double price;

    // ------------------------
    // MEDIA
    // ------------------------
    private MultipartFile image;
    private String imgPath;

    // ------------------------
    // RELATIONS
    // ------------------------
    private List<Long> ingredientIds;
}