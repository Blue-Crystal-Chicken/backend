package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OfferResponse {
    private Long id;
    private String name;
    private String description;
    private String imgPath;
    private Double price;
    private List<MenuResponse> menus;
    private List<OfferProductResponse> offerProducts;
    private LocalDateTime updatedAt;
}
