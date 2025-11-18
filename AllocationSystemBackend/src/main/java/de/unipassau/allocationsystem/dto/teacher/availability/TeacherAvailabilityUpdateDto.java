package de.unipassau.allocationsystem.dto.teacher.availability;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing teacher availability record.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityUpdateDto {

    private Long yearId;

    private Long internshipTypeId;

    private Boolean isAvailable;

    @Positive(message = "Preference rank must be positive")
    private Integer preferenceRank;

    private String notes;
}
