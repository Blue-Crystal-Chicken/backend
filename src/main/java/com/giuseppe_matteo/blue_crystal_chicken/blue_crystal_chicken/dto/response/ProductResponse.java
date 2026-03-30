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

    private String size;
    private Integer quantity;
    private Double liters;

    private Boolean isSpicy;
    private Double weight;
    private String flavor;
    private String temperature;
    private Boolean isCarbonated;

    private Integer calories;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Boolean isGlutenFree;

    private Double additions;
    private Double price;

    private String nutritionalInfo;
    private String imgPath;

    private List<IngredientResponse> ingredients;
    private LocalDateTime updatedAt;
}