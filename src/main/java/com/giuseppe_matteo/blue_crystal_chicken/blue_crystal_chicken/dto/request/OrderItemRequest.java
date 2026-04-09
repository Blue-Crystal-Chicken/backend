package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    private String specialNote;
}
