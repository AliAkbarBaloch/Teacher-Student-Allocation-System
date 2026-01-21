package de.unipassau.allocationsystem.dto.report.teacher;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for comprehensive teacher status report.
 * Aggregates teacher metrics and individual profiles.
 */
@Data
@Builder
public class TeacherStatusReportDto {
    private TeacherMetricsDto metrics;
    private List<TeacherProfileDto> profiles;
}
