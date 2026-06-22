package com.r2u.notification.dto.mapper.Impl;

import org.springframework.stereotype.Component;

import com.r2u.notification.dto.mapper.UserDeviceMapper;
import com.r2u.notification.dto.response.UserDeviceResponse;
import com.r2u.notification.entity.model.UserDevice;

@Component
public class UserDeviceMapperImpl implements UserDeviceMapper {

    @Override
    public UserDeviceResponse toResponse(UserDevice userDevice) {
        if (userDevice == null) {
            return null;
        }
        UserDeviceResponse response = new UserDeviceResponse();
        response.setId(userDevice.getId());
        response.setUserId(userDevice.getUserId());
        if (userDevice.getDevice() != null) {
            response.setDeviceId(userDevice.getDevice().getId());
            response.setDeviceToken(userDevice.getDevice().getDeviceToken());
            response.setPlatform(userDevice.getDevice().getPlatform());
        }
        response.setIsActive(userDevice.getIsActive());
        response.setLastSeenAt(userDevice.getLastSeenAt());
        response.setCreatedAt(userDevice.getCreatedAt());
        response.setUpdatedAt(userDevice.getUpdatedAt());
        return response;
    }
}
