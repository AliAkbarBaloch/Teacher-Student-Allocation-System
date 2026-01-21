package de.unipassau.allocationsystem.dto.subjectcategory;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing subject category.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCategoryUpdateDto {
    @Size(min = 4, message = "Category title must be at least 4 characters")
    private String categoryTitle;
}
