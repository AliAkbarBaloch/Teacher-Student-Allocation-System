package de.unipassau.allocationsystem.dto.academicyear;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for returning AcademicYear information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearResponseDto {
    private Long id;
    private String yearName;
    private Integer totalCreditHours;
    private Integer elementarySchoolHours;
    private Integer middleSchoolHours;
    private LocalDateTime budgetAnnouncementDate;
    private LocalDateTime allocationDeadline;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
