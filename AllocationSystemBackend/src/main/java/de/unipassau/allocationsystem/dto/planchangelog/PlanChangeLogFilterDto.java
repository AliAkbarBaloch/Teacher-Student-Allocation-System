package de.unipassau.allocationsystem.dto.planchangelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering and paginating plan change logs.
 * Supports filtering by plan, change type, entity type, and date range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanChangeLogFilterDto {
    private Long planId;
    private String changeType;
    private String entityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
