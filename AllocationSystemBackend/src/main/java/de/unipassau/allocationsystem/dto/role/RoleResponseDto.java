package de.unipassau.allocationsystem.dto.role;

import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.mapper.util.DtoFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for role response data.
 * Includes all role details with timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create RoleResponseDto from a Role entity.
     * 
     * @param entity The Role entity
     * @return RoleResponseDto or null if entity is null
     */
    public static RoleResponseDto fromEntity(Role entity) {
        return DtoFactory.fromEntity(entity, role -> new RoleResponseDto(
                role.getId(),
                role.getTitle(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        ));
    }
}