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
 * Contains distinct fields instead of JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicFormSubmissionDto {

    @NotNull(message = "School ID is required")
    private Long schoolId;

    @NotNull(message = "Employment status is required")
    @Size(max = 50, message = "Employment status must not exceed 50 characters")
    private String employmentStatus;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @NotEmpty(message = "At least one subject must be selected")
    private List<Long> subjectIds;

    @NotNull(message = "Internship type preference is required")
    @Size(max = 50, message = "Internship type preference must not exceed 50 characters")
    private String internshipTypePreference;

    private List<String> internshipCombinations;

    @NotEmpty(message = "At least one semester availability option must be selected")
    private List<String> semesterAvailability;

    @NotEmpty(message = "At least two availability options must be selected")
    @Size(min = 2, message = "At least two availability options must be selected")
    private List<String> availabilityOptions;
}

