package com.r2u.notification.dto.mapper;

import org.mapstruct.Mapper;

import com.r2u.notification.dto.request.DeviceRequest;
import com.r2u.notification.dto.response.DeviceResponse;
import com.r2u.notification.entity.model.DeviceEntity;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    DeviceEntity toEntity(DeviceRequest deviceRequest);
    DeviceResponse toResponse(DeviceEntity deviceEntity);
}
