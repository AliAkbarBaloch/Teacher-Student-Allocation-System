package de.unipassau.allocationsystem.dto.academicyear;

import java.time.LocalDateTime;

/**
 * Common interface for academic year create and update DTOs.
 * Provides shared getters for common fields used in entity mapping.
 */
public interface AcademicYearUpsertDto {
    /**
     * Gets the year name.
     * @return Year name
     */
    String getYearName();

    /**
     * Gets total credit hours.
     * @return Total credit hours
     */
    Integer getTotalCreditHours();

    /**
     * Gets elementary school hours.
     * @return Elementary school hours
     */
    Integer getElementarySchoolHours();

    /**
     * Gets middle school hours.
     * @return Middle school hours
     */
    Integer getMiddleSchoolHours();

    /**
     * Gets budget announcement date.
     * @return Budget announcement date
     */
    LocalDateTime getBudgetAnnouncementDate();

    /**
     * Gets allocation deadline.
     * @return Allocation deadline
     */
    LocalDateTime getAllocationDeadline();

    /**
     * Gets locked status.
     * @return Lock status flag
     */
    Boolean getIsLocked();
}
