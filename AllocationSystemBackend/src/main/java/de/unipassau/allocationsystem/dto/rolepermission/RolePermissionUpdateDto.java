package de.unipassau.allocationsystem.dto.rolepermission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing role-permission mapping.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionUpdateDto {
    private Long roleId;
    private Long permissionId;
    private String accessLevel;
}
