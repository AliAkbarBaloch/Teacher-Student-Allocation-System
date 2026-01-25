package de.unipassau.allocationsystem.dto.report.allocation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for analyzing teacher utilization across allocation plans.
 * Categorizes teachers by their assignment counts: unassigned, under-utilized, over-utilized, and perfectly utilized.
 */
@Data
@Builder
public class UtilizationAnalysisDto {
    private List<TeacherUtilizationDto> unassignedTeachers; // 0 assignments
    private List<TeacherUtilizationDto> underUtilizedTeachers; // 1 assignment
    private List<TeacherUtilizationDto> overUtilizedTeachers; // > 2 assignments
    private List<TeacherUtilizationDto> perfectlyUtilizedTeachers; // Exactly 2 assignments
}