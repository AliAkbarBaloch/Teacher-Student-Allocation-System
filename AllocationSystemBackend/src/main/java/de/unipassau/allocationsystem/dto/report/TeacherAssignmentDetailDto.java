package de.unipassau.allocationsystem.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherAssignmentDetailDto {
    private Long assignmentId;
    private String teacherName;
    private String schoolName;
    private String schoolZone; // Zone 1, 2, 3
    private String internshipCode; // PDP1, ZSP, etc.
    private String subjectCode;
    private Integer studentGroupSize;
    private String assignmentStatus;
}
