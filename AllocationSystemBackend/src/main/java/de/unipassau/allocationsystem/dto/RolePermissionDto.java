package de.unipassau.allocationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDto {
    private Long id;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    @NotBlank(message = "Access level is required")
    private String accessLevel;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: Include role and permission details in response
    private String roleTitle;
    private String permissionTitle;
}
