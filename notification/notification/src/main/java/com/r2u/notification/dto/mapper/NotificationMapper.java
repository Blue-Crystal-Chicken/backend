package com.r2u.notification.dto.mapper;


import com.r2u.notification.dto.request.NotificationRequest;
import com.r2u.notification.dto.response.NotificationResponse;
import com.r2u.notification.entity.model.NotificationEntity;

import org.mapstruct.Mapper;


@Mapper
public interface NotificationMapper {
    NotificationResponse toResponse(NotificationEntity notificationEntity);
    NotificationEntity toEntity(NotificationRequest notificationRequest);
}
