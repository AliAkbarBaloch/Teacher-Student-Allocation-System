package de.unipassau.allocationsystem.dto.academicyear;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearUpdateDto {
    private String yearName;
    private Integer totalCreditHours;
    private Integer elementarySchoolHours;
    private Integer middleSchoolHours;
    private LocalDateTime budgetAnnouncementDate;
    private LocalDateTime allocationDeadline;
    private Boolean isLocked;
}