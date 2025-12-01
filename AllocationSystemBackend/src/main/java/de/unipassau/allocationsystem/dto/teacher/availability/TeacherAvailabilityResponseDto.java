package de.unipassau.allocationsystem.dto.teacher.availability;

import de.unipassau.allocationsystem.entity.TeacherAvailability.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityResponseDto {
    private Long id;

    private Long teacherId;
    private String teacherFirstName;
    private String teacherLastName;
    private String teacherEmail;

    private Long academicYearId;
    private String academicYearName;

    private Long internshipTypeId;
    private String internshipTypeName;
    private String internshipTypeCode;

    private AvailabilityStatus status;

    private Boolean isAvailable;

    private Integer preferenceRank;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
