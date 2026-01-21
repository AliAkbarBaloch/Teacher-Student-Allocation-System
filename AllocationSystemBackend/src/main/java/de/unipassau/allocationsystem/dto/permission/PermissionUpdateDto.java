package de.unipassau.allocationsystem.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing permission.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateDto {
    @Size(max = 255)
    private String title;  // Optional

    @Size(max = 1000)
    private String description;  // Optional
}
