package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String imgPath;
    private List<MenuProductResponse> menuProducts;
    private LocalDateTime updatedAt;
}
