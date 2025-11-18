package de.unipassau.allocationsystem.dto.subjectcategory;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCategoryResponseDto {
    private Long id;
    private String categoryTitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
