package de.unipassau.allocationsystem.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new subject.
 * Defines subject details including code, title, category, and applicable school type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCreateDto {
    @NotBlank(message = "Subject code is required")
    @Size(min = 1, max = 50, message = "Subject code must be between 1 and 50 characters")
    private String subjectCode;

    @NotBlank(message = "Subject title is required")
    @Size(min = 2, max = 255, message = "Subject title must be between 2 and 255 characters")
    private String subjectTitle;

    @NotNull(message = "Subject category ID is required")
    private Long subjectCategoryId;

    @Size(max = 50, message = "School type must not exceed 50 characters")
    private String schoolType;

    private Boolean isActive;
}

