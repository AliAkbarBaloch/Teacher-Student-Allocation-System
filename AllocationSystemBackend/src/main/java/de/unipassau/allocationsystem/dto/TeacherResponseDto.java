package de.unipassau.allocationsystem.dto;

import de.unipassau.allocationsystem.entity.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private Teacher.EmploymentStatus employmentStatus;
    private Teacher.UsageCycle usageCycle;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
