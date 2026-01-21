package de.unipassau.allocationsystem.dto.internshipdemand;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new internship demand record.
 * Represents the need for teachers in specific subjects, school types, and internship types.
 */
@Getter
@Setter
@NoArgsConstructor
public class InternshipDemandCreateDto {
    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotNull
    private Long internshipTypeId;

    @NotNull
    private String schoolType;

    @NotNull
    private Long subjectId;

    @NotNull
    @Min(0)
    private Integer requiredTeachers;

    @Min(0)
    private Integer studentCount;

    private Boolean isForecasted = Boolean.FALSE;
}