package de.unipassau.allocationsystem.dto.report.allocation;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for detailed teacher assignment information in reports.
 * Includes teacher details, school location, internship type, and assignment status.
 */
@Data
@Builder
public class TeacherAssignmentDetailDto {
    private Long assignmentId;
    private String teacherName;
    private String teacherEmail;
    private String schoolName;
    private String schoolZone; // Zone 1, 2, 3
    private String internshipCode; // PDP1, ZSP, etc.
    private String subjectCode;
    private Integer studentGroupSize;
    private String assignmentStatus;
}
