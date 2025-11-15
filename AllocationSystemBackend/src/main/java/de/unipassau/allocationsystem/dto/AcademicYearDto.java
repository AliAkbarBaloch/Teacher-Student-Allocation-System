package de.unipassau.allocationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearDto {
    private Long id;

    @NotBlank(message = "Year is required")
    private String yearName;

    @NotBlank(message = "Total Credit Hours is required")
    private Integer totalCreditHours;

    @NotBlank(message = "Elementary School Hours is required")
    private Integer elementarySchoolHours;

    @NotBlank(message = "Middle School Hours is required")
    private Integer middleSchoolHours;

    @NotBlank(message = "Middle School Hours is required")
    private LocalDateTime budgetAnnouncementDate;

    private LocalDateTime allocationDeadline;

    private Boolean isLocked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
