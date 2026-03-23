package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private String category;
    private String size;
    private Integer quantity;
    private Double additions;
    private Double price;
    private String nutritionalInfo;
    private List<Long> ingredientIds;
}
