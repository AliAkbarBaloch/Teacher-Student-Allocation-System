package de.unipassau.allocationsystem.dto.school;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating school status (activate/deactivate).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolStatusUpdateDto {

    @NotNull(message = "isActive status is required")
    private Boolean isActive;
}
