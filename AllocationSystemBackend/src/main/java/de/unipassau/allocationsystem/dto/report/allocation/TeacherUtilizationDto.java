package de.unipassau.allocationsystem.dto.report.allocation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherUtilizationDto {
    private Long teacherId;
    private String teacherName;
    private String email;
    private String schoolName;
    private int assignmentCount;
    private String notes; // e.g., "Owes 1 assignment from previous year"
}
