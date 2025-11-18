package de.unipassau.allocationsystem.dto.rolepermission;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for returning RolePermission information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionResponseDto {
    private Long id;
    private Long roleId;
    private Long permissionId;
    private String accessLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: Include role and permission details in response
    private String roleTitle;
    private String permissionTitle;
}
