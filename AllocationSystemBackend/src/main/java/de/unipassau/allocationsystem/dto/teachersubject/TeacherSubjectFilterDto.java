package de.unipassau.allocationsystem.dto.teachersubject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering and paginating teacher-subject mappings.
 * Supports filtering by academic year, teacher, subject, availability status, and grade levels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectFilterDto {
    private Long academicYearId;
    private Long teacherId;
    private Long subjectId;
    private String availabilityStatus;
    private Integer gradeLevelFrom;
    private Integer gradeLevelTo;

    // Pagination
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
