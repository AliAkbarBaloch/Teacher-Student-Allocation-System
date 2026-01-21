package de.unipassau.allocationsystem.dto.report.teacher;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for individual teacher utilization report.
 * Tracks current assignments, credit balance, and utilization status.
 */
@Data
@Builder
public class TeacherUtilizationReportDto {
    private Long teacherId;
    private String teacherName;
    private String schoolName;

    // Assignment Data
    private int assignmentsInCurrentPlan; // Should be 2 ideally

    // Historical Data
    private double currentCreditBalance; // From CreditHourTracking

    // Status
    private String utilizationStatus; // "UNDER_UTILIZED", "OPTIMAL", "OVER_UTILIZED"
    private boolean isUnused;
}