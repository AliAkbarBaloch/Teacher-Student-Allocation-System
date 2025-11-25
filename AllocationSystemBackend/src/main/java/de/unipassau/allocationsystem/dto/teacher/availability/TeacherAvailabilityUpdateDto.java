package de.unipassau.allocationsystem.dto.teacher.availability;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing teacher availability record.
 * Permission-style controller requires teacherId in the request body for update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityUpdateDto {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private Long yearId;

    private Long internshipTypeId;

    private Boolean isAvailable;

    @Positive(message = "Preference rank must be positive")
    private Integer preferenceRank;

    private String notes;
}
