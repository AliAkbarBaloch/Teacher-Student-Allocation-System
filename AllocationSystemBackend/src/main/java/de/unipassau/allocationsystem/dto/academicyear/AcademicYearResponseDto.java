package de.unipassau.allocationsystem.dto.academicyear;

import de.unipassau.allocationsystem.entity.AcademicYear;
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

    /**
     * Creates response DTO from entity.
     * 
     * @param entity Source academic year entity
     * @return Response DTO or null if entity is null
     */
    public static AcademicYearResponseDto fromEntity(AcademicYear entity) {
        if (entity == null) {
            return null;
        }
        return new AcademicYearResponseDto(
                entity.getId(),
                entity.getYearName(),
                entity.getTotalCreditHours(),
                entity.getElementarySchoolHours(),
                entity.getMiddleSchoolHours(),
                entity.getBudgetAnnouncementDate(),
                entity.getAllocationDeadline(),
                entity.getIsLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
