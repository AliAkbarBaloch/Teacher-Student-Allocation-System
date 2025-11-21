package de.unipassau.allocationsystem.dto.teachersubject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectResponseDto {
    private Long id;
    private Long yearId;
    private Long teacherId;
    private Long subjectId;
    private String availabilityStatus;
    private Integer gradeLevelFrom;
    private Integer gradeLevelTo;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
