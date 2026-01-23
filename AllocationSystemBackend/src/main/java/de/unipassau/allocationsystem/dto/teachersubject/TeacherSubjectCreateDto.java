package de.unipassau.allocationsystem.dto.teachersubject;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new teacher-subject mapping.
 * Defines which subjects a teacher is qualified to teach with grade level specifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectCreateDto {
    private Long academicYearId;
    // teacherId is provided via the path variable in the controller, removed from body
    private Integer gradeLevelFrom;
    private Integer gradeLevelTo;
    private String notes;
    @NotNull
    private Long teacherId;
    @NotNull
    private Long subjectId;
    @NotNull
    private String availabilityStatus;
}
