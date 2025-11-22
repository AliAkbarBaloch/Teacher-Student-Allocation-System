package de.unipassau.allocationsystem.dto.teacher.formsubmission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for generating a form link for a teacher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormLinkGenerateRequestDto {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Academic year ID is required")
    private Long yearId;
}


