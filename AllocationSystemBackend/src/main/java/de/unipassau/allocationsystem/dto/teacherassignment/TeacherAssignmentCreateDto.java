package de.unipassau.allocationsystem.dto.teacherassignment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new teacher assignment.
 * Associates a teacher with a specific internship type and subject within an allocation plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentCreateDto {

    @NotNull(message = "planId is required")
    private Long planId;

    @NotNull(message = "teacherId is required")
    private Long teacherId;

    @NotNull(message = "internshipTypeId is required")
    private Long internshipTypeId;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    @NotNull(message = "studentGroupSize is required")
    @Min(value = 1, message = "studentGroupSize must be at least 1")
    private Integer studentGroupSize = 1;

    @NotNull(message = "assignmentStatus is required")
    @Size(min = 1)
    private String assignmentStatus;

    private Boolean isManualOverride = false;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}