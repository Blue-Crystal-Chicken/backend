package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UpdateOrderRequest {
    private String serviceType;
    private String orderType;
    private String tableNumber;
    private String paymentType;
    private String status;
    private List<OrderItemRequest> items;
}
