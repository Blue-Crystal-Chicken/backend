package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.IngredientMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OrderMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderItemResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OrderResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapperImpl implements OrderMapper {

    private final IngredientMapper ingredientMapper;

    @Override
    public OrderEntity toEntity(OrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(UUID.randomUUID().toString());
        entity.setServiceType(request.getServiceType());
        entity.setOrderType(request.getOrderType());
        entity.setTableNumber(request.getTableNumber());
        entity.setPaymentType(request.getPaymentType());
        entity.setStatus(OrderStatus.PENDING);
        return entity;
    }

    @Override
    public OrderResponse toResponse(OrderEntity entity) {
        OrderResponse response = new OrderResponse();
        response.setId(entity.getId());
        response.setOrderId(entity.getOrderId());
        response.setCode(entity.getCode());
        response.setServiceType(entity.getServiceType());
        response.setOrderType(entity.getOrderType());
        response.setTableNumber(entity.getTableNumber());
        response.setPaymentType(entity.getPaymentType());
        response.setPaymentAmount(entity.getPaymentAmount());
        response.setTotalAt(entity.getTotalAt());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
            response.setUserName(entity.getUser().getName() + " " + entity.getUser().getSurname());
        }
        if (entity.getLocation() != null) {
            response.setLocationId(entity.getLocation().getId());
        }
        if (entity.getOrderProducts() != null) {
            response.setItems(entity.getOrderProducts().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    @Override
    public OrderItemResponse toItemResponse(OrderProduct op) {
        OrderItemResponse item = new OrderItemResponse();
        
        if (op.getProduct() != null) {
            item.setProductId(op.getProduct().getId());
            item.setProductName(op.getProduct().getName());
        }
        
        if (op.getOffer() != null) {
            item.setOfferId(op.getOffer().getId());
            item.setOfferName(op.getOffer().getName());
        }
        
        item.setQuantity(op.getQuantity());
        item.setPrice(op.getPrice());
        item.setAdditions(op.getAdditions());
        item.setSpecialNote(op.getSpecialNote());

        if (op.getIngredients() != null) {
            item.setIngredients(op.getIngredients().stream()
                    .map(ingredientMapper::toResponse)
                    .collect(Collectors.toList()));
        }

        return item;
    }
}
