package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the processing status of a teacher form submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherFormSubmissionStatusUpdateDto {

    @NotNull(message = "Processing status is required")
    private Boolean isProcessed;
}
