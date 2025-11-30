package de.unipassau.allocationsystem.dto.teacher;

import de.unipassau.allocationsystem.entity.Teacher;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating teacher status (activate/deactivate).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStatusUpdateDto {

    @NotNull(message = "Employment status is required")
    private Teacher.EmploymentStatus employmentStatus;
}
