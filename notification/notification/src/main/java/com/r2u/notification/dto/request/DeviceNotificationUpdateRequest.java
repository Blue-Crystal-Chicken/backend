package com.r2u.notification.dto.request;

import com.r2u.notification.entity.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceNotificationUpdateRequest {

    private Long id;
    private Status status;
    
    
}
