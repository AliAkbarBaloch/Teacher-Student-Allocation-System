package de.unipassau.allocationsystem.dto.teachersubject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectUpdateDto {
    private String availabilityStatus;
    private Integer gradeLevelFrom;
    private Integer gradeLevelTo;
    private String notes;
}
