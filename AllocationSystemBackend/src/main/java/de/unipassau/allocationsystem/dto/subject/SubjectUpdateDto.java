package de.unipassau.allocationsystem.dto.subject;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectUpdateDto {
    @Size(min = 2, max = 50, message = "Subject code must be between 2 and 50 characters")
    private String subjectCode;

    @Size(min = 2, max = 255, message = "Subject title must be between 2 and 255 characters")
    private String subjectTitle;

    private Long subjectCategoryId;

    @Size(max = 50, message = "School type must not exceed 50 characters")
    private String schoolType;

    private Boolean isActive;
}

