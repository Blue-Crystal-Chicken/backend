package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;

    private List<OrderItemRequest> items;

    private String code;

    private String paymentType;

    private String serviceType;

    private String orderType;

    private String tableNumber;

    private Long locationId;
}