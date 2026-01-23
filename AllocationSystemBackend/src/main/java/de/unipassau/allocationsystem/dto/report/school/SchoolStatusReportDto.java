package de.unipassau.allocationsystem.dto.report.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for school status report.
 * Provides aggregated metrics and detailed profiles for all schools.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolStatusReportDto {
    private SchoolMetricsDto metrics;
    private List<SchoolProfileDto> profiles;
}
