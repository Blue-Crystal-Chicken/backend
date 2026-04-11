package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;

@Data
public class MenuProductResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Boolean obligatory;
    private Double unitPrice;
}
