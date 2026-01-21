package de.unipassau.allocationsystem.dto.subjectcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new subject category.
 * Organizes subjects into logical groupings (e.g., Languages, Sciences).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCategoryCreateDto {
    @NotBlank(message = "Category title is required")
    @Size(min = 4, message = "Category title must be at least 4 characters")
    private String categoryTitle;
}
