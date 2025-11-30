package de.unipassau.allocationsystem.dto.internshipdemand;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternshipDemandResponseDto {
    private Long id;
    private Long yearId;
    private Long internshipTypeId;
    private String internshipTypeCode;
    private String schoolType;
    private Long subjectId;
    private String subjectCode;
    private Integer requiredTeachers;
    private Integer studentCount;
    private Boolean isForecasted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
