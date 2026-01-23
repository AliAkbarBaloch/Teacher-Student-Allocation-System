package de.unipassau.allocationsystem.dto.rolepermission;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new role-permission mapping.
 * Associates a specific permission with a role at a defined access level.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionCreateDto {
    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    @NotBlank(message = "Access level is required")
    private String accessLevel;
}
