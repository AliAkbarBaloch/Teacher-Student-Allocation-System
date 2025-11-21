package de.unipassau.allocationsystem.dto.teachersubject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectFilterDto {
    private Long teacherId;
    private Long yearId;
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
