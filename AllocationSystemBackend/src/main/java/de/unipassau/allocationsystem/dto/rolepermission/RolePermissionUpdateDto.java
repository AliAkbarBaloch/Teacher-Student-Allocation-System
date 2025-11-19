package de.unipassau.allocationsystem.dto.rolepermission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionUpdateDto {
    private Long roleId;
    private Long permissionId;
    private String accessLevel;
}
