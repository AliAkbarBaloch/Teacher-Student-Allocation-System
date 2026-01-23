package de.unipassau.allocationsystem.dto.report.allocation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for report header information.
 * Contains metadata about the allocation plan including name, version, and generation timestamp.
 */
@Data
@Builder
public class ReportHeaderDto {
    private String planName;
    private String planVersion;
    private String academicYear;
    private String status;
    private LocalDateTime generatedAt;
}
