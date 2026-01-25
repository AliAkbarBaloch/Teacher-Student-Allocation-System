package de.unipassau.allocationsystem.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new permission.
 * Defines access control permissions for the role-based access system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCreateDto implements PermissionUpsertDto {
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000)
    private String description;
}
