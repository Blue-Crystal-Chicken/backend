package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderId;
    private String code;
    private String serviceType;
    private String orderType;
    private String tableNumber;
    private String paymentType;
    private BigDecimal paymentAmount;
    private BigDecimal totalAt;
    private String status;
    private Long userId;
    private String userName;
    private Long locationId;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
