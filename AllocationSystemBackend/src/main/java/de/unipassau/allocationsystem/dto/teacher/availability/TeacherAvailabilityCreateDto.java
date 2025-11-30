package de.unipassau.allocationsystem.dto.teacher.availability;

import de.unipassau.allocationsystem.entity.TeacherAvailability;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new teacher availability record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityCreateDto {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotNull(message = "Internship type ID is required")
    private Long internshipTypeId;

    @NotNull(message = "Availability status is required")
    private TeacherAvailability.AvailabilityStatus status;

    @Positive(message = "Preference rank must be positive")
    private Integer preferenceRank;

    private String notes;
}
