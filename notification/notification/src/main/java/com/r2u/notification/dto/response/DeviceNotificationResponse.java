package com.r2u.notification.dto.response;

import java.time.LocalDateTime;

import com.r2u.notification.entity.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceNotificationResponse {

    private Long id;
    private Long notificationId;
    private String deviceTokenRequest;
    private String deviceToken;
    private Status status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
