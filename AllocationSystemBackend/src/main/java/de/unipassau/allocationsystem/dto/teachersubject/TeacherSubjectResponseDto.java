package de.unipassau.allocationsystem.dto.teachersubject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for teacher-subject mapping response data.
 * Includes detailed academic year, teacher, and subject information with grade levels and timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectResponseDto {
    private Long id;
    private Long academicYearId;
    private String academicYearTitle;
    private Long teacherId;
    private String teacherTitle;
    private Long subjectId;
    private String subjectTitle;
    private String availabilityStatus;
    private Integer gradeLevelFrom;
    private Integer gradeLevelTo;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
