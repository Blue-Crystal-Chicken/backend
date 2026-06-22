package com.r2u.notification.dto.mapper.Impl;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2u.notification.dto.mapper.NotificationMapper;
import com.r2u.notification.dto.request.NotificationRequest;
import com.r2u.notification.dto.response.NotificationResponse;
import com.r2u.notification.entity.enums.Type;
import com.r2u.notification.entity.model.NotificationEntity;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapperImpl implements NotificationMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NotificationResponse toResponse(NotificationEntity notificationEntity) {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setId(notificationEntity.getId());
        notificationResponse.setTitle(notificationEntity.getTitle());
        notificationResponse.setBody(notificationEntity.getBody());
        notificationResponse.setType(notificationEntity.getType().name());
        notificationResponse.setCreatedAt(notificationEntity.getCreatedAt());
        notificationResponse.setUpdatedAt(notificationEntity.getUpdatedAt());

        if (notificationEntity.getPayload() != null) {
            Map<String, Object> payloadMap = objectMapper.convertValue(
                notificationEntity.getPayload(), 
                new TypeReference<Map<String, Object>>() {}
            );
            notificationResponse.setPayload(payloadMap);
        }

        return notificationResponse;
    }

    @Override
    public NotificationEntity toEntity(NotificationRequest notificationRequest) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setTitle(notificationRequest.getTitle());
        notificationEntity.setBody(notificationRequest.getBody());
        notificationEntity.setType(Type.valueOf(notificationRequest.getType().toUpperCase()));

        if (notificationRequest.getPayload() != null) {
            notificationEntity.setPayload(
                objectMapper.valueToTree(notificationRequest.getPayload())
            );
        }

        return notificationEntity;
    }
}