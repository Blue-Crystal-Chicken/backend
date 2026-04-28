package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Long offerId;
    private String offerName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal additions;
    private String specialNote;
    private List<IngredientResponse> ingredients;
}
