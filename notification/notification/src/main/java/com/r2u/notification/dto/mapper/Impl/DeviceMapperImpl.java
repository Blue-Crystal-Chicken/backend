package com.r2u.notification.dto.mapper.Impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.r2u.notification.dto.mapper.DeviceMapper;
import com.r2u.notification.dto.request.DeviceRequest;
import com.r2u.notification.dto.response.DeviceResponse;
import com.r2u.notification.entity.model.DeviceEntity;

@Component
public class DeviceMapperImpl implements DeviceMapper {

    @Override
    public DeviceEntity toEntity(DeviceRequest deviceRequest) {
        if (deviceRequest == null) {
            return null;
        }
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setDeviceToken(deviceRequest.getToken());
        deviceEntity.setPlatform(deviceRequest.getPlatform());
        deviceEntity.setCreatedAt(LocalDateTime.now());
        deviceEntity.setUpdatedAt(LocalDateTime.now());
        return deviceEntity;
    }

    @Override
    public DeviceResponse toResponse(DeviceEntity deviceEntity) {
        if (deviceEntity == null) {
            return null;
        }
        DeviceResponse deviceResponse = new DeviceResponse();
        deviceResponse.setId(deviceEntity.getId());
        deviceResponse.setToken(deviceEntity.getDeviceToken());
        deviceResponse.setPlatform(deviceEntity.getPlatform());
        deviceResponse.setCreatedAt(deviceEntity.getCreatedAt());
        deviceResponse.setUpdatedAt(deviceEntity.getUpdatedAt());
        return deviceResponse;
    }
}
