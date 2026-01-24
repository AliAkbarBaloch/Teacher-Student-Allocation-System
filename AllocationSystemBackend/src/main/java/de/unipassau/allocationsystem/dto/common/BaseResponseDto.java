package de.unipassau.allocationsystem.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base response DTO with common fields (id, timestamps).
 * Provides a foundation for response DTOs with standard entity mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
