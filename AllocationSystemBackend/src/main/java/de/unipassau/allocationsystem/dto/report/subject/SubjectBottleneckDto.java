package de.unipassau.allocationsystem.dto.report.subject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectBottleneckDto {
    private String subjectName;
    private String schoolType;
    private int requiredTeacherCount;  // From InternshipDemand
    private int availableTeacherCount; // Teachers qualified + available
    private int actuallyAssignedCount; // In current plan

    private int gap; // available - required
    private String status; // "CRITICAL_SHORTAGE", "SHORTAGE", "BALANCED", "SURPLUS"
}
