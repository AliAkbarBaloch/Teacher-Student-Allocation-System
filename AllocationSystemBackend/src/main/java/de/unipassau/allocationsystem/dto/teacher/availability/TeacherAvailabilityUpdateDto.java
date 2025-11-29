package de.unipassau.allocationsystem.dto.teacher.availability;

import de.unipassau.allocationsystem.entity.TeacherAvailability.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityUpdateDto {
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private Long academicYearId;

    private Long internshipTypeId;

    private AvailabilityStatus status;

    @Positive(message = "Preference rank must be positive")
    private Integer preferenceRank;

    private String notes;
}
