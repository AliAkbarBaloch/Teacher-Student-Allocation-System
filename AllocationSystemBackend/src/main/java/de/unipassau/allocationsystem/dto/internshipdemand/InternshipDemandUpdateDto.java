package de.unipassau.allocationsystem.dto.internshipdemand;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing internship demand record.
 * All fields except academic year ID are optional for partial updates.
 */
@Getter
@Setter
@NoArgsConstructor
public class InternshipDemandUpdateDto {
    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;
    private Long internshipTypeId;
    private String schoolType;
    private Long subjectId;

    @Min(0)
    private Integer requiredTeachers;

    @Min(0)
    private Integer studentCount;

    private Boolean isForecasted;
}