package com.r2u.notification.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.r2u.notification.dto.request.DeviceNotificationRequest;
import com.r2u.notification.dto.response.DeviceNotificationResponse;
import com.r2u.notification.entity.model.DeviceNotification;
import com.r2u.notification.entity.model.NotificationEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceNotificationMapper {
    
    DeviceNotificationResponse toResponse(DeviceNotification deviceNotification);

    DeviceNotification toEntity(DeviceNotificationRequest deviceNotificationRequest, NotificationEntity notification);
    
}
