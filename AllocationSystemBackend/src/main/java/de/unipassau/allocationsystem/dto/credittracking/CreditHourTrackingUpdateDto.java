package de.unipassau.allocationsystem.dto.credittracking;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing credit hour tracking record.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditHourTrackingUpdateDto implements CreditHourTrackingUpsertDto {
    private Long teacherId;
    private Long academicYearId;
    @Min(0)
    private Integer assignmentsCount;
    @Min(0)
    private Double creditHoursAllocated;
    @Min(0)
    private Double creditBalance;
    private String notes;
}