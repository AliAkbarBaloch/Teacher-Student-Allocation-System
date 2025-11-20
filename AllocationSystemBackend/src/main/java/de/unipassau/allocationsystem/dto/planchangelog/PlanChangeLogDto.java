package de.unipassau.allocationsystem.dto.planchangelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanChangeLogDto {
    private Long id;
    private Long planId;
    private Long userId;
    private String changeType;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private LocalDateTime eventTimestamp;
    private String reason;
}
