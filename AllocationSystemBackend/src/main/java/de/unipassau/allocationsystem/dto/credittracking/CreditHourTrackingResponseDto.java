package de.unipassau.allocationsystem.dto.credittracking;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for credit hour tracking response data.
 * Includes teacher and academic year details with assignment and credit information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditHourTrackingResponseDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long academicYearId;
    private String academicYearTitle;
    private Integer assignmentsCount;
    private Double creditHoursAllocated;
    private Double creditBalance;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
