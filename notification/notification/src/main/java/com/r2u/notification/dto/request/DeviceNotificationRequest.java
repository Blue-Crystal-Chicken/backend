package com.r2u.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceNotificationRequest {

    private Long notificationId;
    private String deviceToken;
    private String deviceTokenRequest;

}
