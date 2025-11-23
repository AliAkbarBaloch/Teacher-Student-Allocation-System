package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new teacher form submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherFormSubmissionCreateDto {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Academic year ID is required")
    private Long yearId;

    @NotBlank(message = "Form token is required")
    private String formToken;

    @NotNull(message = "Submission date is required")
    private LocalDateTime submittedAt;

    // Submission data fields
    private Long schoolId;
    private String employmentStatus;
    private String notes;
    private java.util.List<Long> subjectIds;
    private String internshipTypePreference;
    private java.util.List<String> internshipCombinations;
    private java.util.List<String> semesterAvailability;
    private java.util.List<String> availabilityOptions;
}
