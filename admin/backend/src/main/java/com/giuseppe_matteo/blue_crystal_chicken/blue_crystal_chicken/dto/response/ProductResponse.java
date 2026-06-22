package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String category;    
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
    private String imgPath;

    // ------------------------
    // INGREDIENTS
    // ------------------------
    private List<IngredientResponse> ingredients;
    
    private LocalDateTime updatedAt;
}