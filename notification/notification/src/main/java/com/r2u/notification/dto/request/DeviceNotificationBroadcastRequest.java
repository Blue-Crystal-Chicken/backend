package com.r2u.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceNotificationBroadcastRequest {
    private Long notificationId;
    private String deviceTokenRequest;
}
