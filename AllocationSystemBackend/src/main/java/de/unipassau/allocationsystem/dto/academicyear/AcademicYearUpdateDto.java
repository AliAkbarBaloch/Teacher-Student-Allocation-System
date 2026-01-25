package de.unipassau.allocationsystem.dto.academicyear;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing academic year.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearUpdateDto implements AcademicYearUpsertDto {
    private String yearName;
    private Integer totalCreditHours;
    private Integer elementarySchoolHours;
    private Integer middleSchoolHours;
    private LocalDateTime budgetAnnouncementDate;
    private LocalDateTime allocationDeadline;
    private Boolean isLocked;
}