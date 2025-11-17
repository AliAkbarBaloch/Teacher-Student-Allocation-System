package de.unipassau.allocationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCategoryDto {
    private Long id;

    @NotBlank(message = "Category title is required")
    @Size(min = 4, message = "Category title must be at least 4 characters")
    private String categoryTitle;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

