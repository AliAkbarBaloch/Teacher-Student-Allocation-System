package de.unipassau.allocationsystem.dto.teacherassignment;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentResponseDto {
    private Long id;
    private Long planId;
    private Long teacherId;
    private Long internshipTypeId;
    private Long subjectId;
    private Integer studentGroupSize;
    private String assignmentStatus;
    private Boolean isManualOverride;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}