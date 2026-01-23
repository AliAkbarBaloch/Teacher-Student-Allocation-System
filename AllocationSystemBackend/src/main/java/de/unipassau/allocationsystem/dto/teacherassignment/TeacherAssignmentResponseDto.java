package de.unipassau.allocationsystem.dto.teacherassignment;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for teacher assignment response data.
 * Includes complete assignment details with plan, teacher, internship, and subject information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentResponseDto {
    private Long id;
    private Long planId;
    private String planTitle;
    private Long teacherId;
    private String teacherTitle;
    private Long internshipTypeId;
    private String internshipTypeTitle;
    private Long subjectId;
    private String subjectTitle;
    private Integer studentGroupSize;
    private String assignmentStatus;
    private Boolean isManualOverride;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}