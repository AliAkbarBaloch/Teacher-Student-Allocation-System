package de.unipassau.allocationsystem.dto;

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
    private Long yearId;

    @NotNull(message = "Internship type ID is required")
    private Long internshipTypeId;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @Positive(message = "Preference rank must be positive")
    private Integer preferenceRank;

    private String notes;
}
