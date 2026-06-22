package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferProductRequest {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private String productImagePath;
    
}
