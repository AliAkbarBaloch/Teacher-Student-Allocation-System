package de.unipassau.allocationsystem.dto.academicyear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearCreateDto {
    @NotBlank(message = "Year is required")
    private String yearName;

    @NotNull(message = "Total Credit Hours is required")
    private Integer totalCreditHours;

    @NotNull(message = "Elementary School Hours is required")
    private Integer elementarySchoolHours;

    @NotNull(message = "Middle School Hours is required")
    private Integer middleSchoolHours;

    @NotNull(message = "Budget Announcement Date is required")
    private LocalDateTime budgetAnnouncementDate;

    @NotNull(message = "Allocation Deadline is required")
    private LocalDateTime allocationDeadline;

    private Boolean isLocked;
}
