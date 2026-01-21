package de.unipassau.allocationsystem.dto.credittracking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new credit hour tracking record.
 * Tracks teacher assignments and credit hours allocated per academic year.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditHourTrackingCreateDto {
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotNull(message = "Assignments count is required")
    @Min(0)
    private Integer assignmentsCount;

    @NotNull(message = "Credit hours allocated is required")
    @Min(0)
    private Double creditHoursAllocated;

    @Min(0)
    private Double creditBalance;

    private String notes;
}