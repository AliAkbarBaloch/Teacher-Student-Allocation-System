package de.unipassau.allocationsystem.dto.permission;

import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.mapper.util.DtoFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for permission response data.
 * Includes all permission details with timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create PermissionResponseDto from a Permission entity.
     * 
     * @param entity The Permission entity
     * @return PermissionResponseDto or null if entity is null
     */
    public static PermissionResponseDto fromEntity(Permission entity) {
        return DtoFactory.fromEntity(entity, permission -> new PermissionResponseDto(
                permission.getId(),
                permission.getTitle(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        ));
    }
}
