package de.unipassau.allocationsystem.dto.subject;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponseDto {
    private Long id;
    private String subjectCode;
    private String subjectTitle;
    private Long subjectCategoryId;
    private String subjectCategoryTitle;
    private String schoolType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

