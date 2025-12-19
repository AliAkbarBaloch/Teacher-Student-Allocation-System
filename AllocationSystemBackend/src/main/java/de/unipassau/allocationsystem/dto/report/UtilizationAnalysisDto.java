package de.unipassau.allocationsystem.dto.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UtilizationAnalysisDto {
    private List<TeacherUtilizationDto> unassignedTeachers; // 0 assignments
    private List<TeacherUtilizationDto> underUtilizedTeachers; // 1 assignment
    private List<TeacherUtilizationDto> overUtilizedTeachers; // > 2 assignments
    private List<TeacherUtilizationDto> perfectlyUtilizedTeachers; // Exactly 2 assignments
}