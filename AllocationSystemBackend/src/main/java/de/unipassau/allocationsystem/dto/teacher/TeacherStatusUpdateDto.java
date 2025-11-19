package de.unipassau.allocationsystem.dto.teacher;

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

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
