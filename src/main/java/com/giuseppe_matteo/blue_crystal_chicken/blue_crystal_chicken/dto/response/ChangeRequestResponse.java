package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestResponse {
    private Long id;
    private String type;
    private String status;
    private Long targetId;
    private String payload;
    private String summary;
    private Long requestedById;
    private String requestedByEmail;
    private Long locationId;
    private String locationName;
    private Long resolvedById;
    private String resolutionNote;
    private String resultMessage;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
