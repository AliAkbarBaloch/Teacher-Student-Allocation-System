package de.unipassau.allocationsystem.dto.subject;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing subject.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectUpdateDto {
    @Size(min = 1, max = 50, message = "Subject code must be between 1 and 50 characters")
    private String subjectCode;

    @Size(min = 2, max = 255, message = "Subject title must be between 2 and 255 characters")
    private String subjectTitle;

    private Long subjectCategoryId;

    @Size(max = 50, message = "School type must not exceed 50 characters")
    private String schoolType;

    private Boolean isActive;
}

