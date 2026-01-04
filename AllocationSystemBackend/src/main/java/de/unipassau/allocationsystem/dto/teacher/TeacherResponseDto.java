package de.unipassau.allocationsystem.dto.teacher;

import java.time.LocalDateTime;
import java.util.List;

import de.unipassau.allocationsystem.dto.subject.SubjectSimpleDto;
import de.unipassau.allocationsystem.entity.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for teacher response data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponseDto {
    private Long id;
    private Long schoolId;
    private String schoolName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean isPartTime;
    private Integer workingHoursPerWeek;
    private Teacher.EmploymentStatus employmentStatus;
    private Teacher.UsageCycle usageCycle;
    private Integer creditHourBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<SubjectSimpleDto> subjects; 
}

