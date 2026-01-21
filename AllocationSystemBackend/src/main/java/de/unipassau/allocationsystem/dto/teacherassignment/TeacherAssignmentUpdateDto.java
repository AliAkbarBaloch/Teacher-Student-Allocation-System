package de.unipassau.allocationsystem.dto.teacherassignment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing teacher assignment.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentUpdateDto {

    @Min(value = 1, message = "studentGroupSize must be at least 1")
    private Integer studentGroupSize;

    @Size(min = 1)
    private String assignmentStatus;

    private Boolean isManualOverride;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}