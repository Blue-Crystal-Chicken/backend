package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;

@Data
public class IngredientResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double quantity;
}
