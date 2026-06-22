package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderItemResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;

public interface OrderMapper {

    OrderEntity toEntity(OrderRequest orderRequest);

    OrderResponse toResponse(OrderEntity orderEntity);

    OrderItemResponse toItemResponse(OrderProduct orderProduct);
}
