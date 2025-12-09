package de.unipassau.allocationsystem.dto.report;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportHeaderDto {
    private String planName;
    private String planVersion;
    private String academicYear;
    private String status;
    private LocalDateTime generatedAt;
}
