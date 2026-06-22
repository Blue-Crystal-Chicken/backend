package com.r2u.notification.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.r2u.notification.dto.response.UserDeviceResponse;
import com.r2u.notification.entity.model.UserDevice;

@Mapper(componentModel = "spring")
public interface UserDeviceMapper {

    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.deviceToken", target = "deviceToken")
    @Mapping(source = "device.platform", target = "platform")
    UserDeviceResponse toResponse(UserDevice userDevice);
}
