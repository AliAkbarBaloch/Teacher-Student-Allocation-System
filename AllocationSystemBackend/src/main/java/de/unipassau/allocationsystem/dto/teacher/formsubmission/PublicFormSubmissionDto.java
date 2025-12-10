package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for public form submission (by teacher).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicFormSubmissionDto {

    @NotNull(message = "School ID is required")
    private Long schoolId;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @NotEmpty(message = "At least one subject must be selected")
    private List<Long> subjectIds;

    @NotEmpty(message = "At least one internship type must be selected")
    private List<Long> internshipTypeIds;
}

