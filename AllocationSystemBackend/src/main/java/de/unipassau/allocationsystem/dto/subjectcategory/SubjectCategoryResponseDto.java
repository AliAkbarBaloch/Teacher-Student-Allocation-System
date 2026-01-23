package de.unipassau.allocationsystem.dto.subjectcategory;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for subject category response data.
 * Includes category details with timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCategoryResponseDto {
    private Long id;
    private String categoryTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
