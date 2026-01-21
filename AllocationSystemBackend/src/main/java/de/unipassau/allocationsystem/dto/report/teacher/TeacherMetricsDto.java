package de.unipassau.allocationsystem.dto.report.teacher;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * DTO for aggregated teacher metrics.
 * Provides counts by status, employment type, and subject coverage breakdown.
 */
@Data
@Builder
public class TeacherMetricsDto {
    private int totalTeachers;
    private int activeCount;
    private int onLeaveCount;
    private int inactiveCount;
    private int partTimeCount;
    private int fullTimeCount;
    // e.g., "Math": 10 teachers, "German": 15 teachers
    private Map<String, Integer> subjectCoverageCounts;
}