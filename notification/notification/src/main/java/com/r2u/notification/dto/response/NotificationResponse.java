package com.r2u.notification.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String body;
    private String type;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    

}
