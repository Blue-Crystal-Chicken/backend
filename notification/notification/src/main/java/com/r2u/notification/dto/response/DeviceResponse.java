package com.r2u.notification.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponse {
    private Long id;
    private String token;
    private String platform;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
