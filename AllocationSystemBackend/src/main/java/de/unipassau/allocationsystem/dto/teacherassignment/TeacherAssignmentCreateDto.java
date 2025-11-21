package de.unipassau.allocationsystem.dto.teacherassignment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherAssignmentCreateDto {

    @NotNull(message = "teacherId is required")
    private Long teacherId;

    @NotNull(message = "internshipTypeId is required")
    private Long internshipTypeId;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    @Min(value = 1, message = "studentGroupSize must be at least 1")
    private Integer studentGroupSize = 1;

    @NotNull(message = "assignmentStatus is required")
    @Size(min = 1)
    private String assignmentStatus;

    private Boolean isManualOverride = false;

    private String notes;
}
