package de.unipassau.allocationsystem.dto.credittracking;

/**
 * Common interface for credit hour tracking create and update DTOs.
 * Provides shared getters for common fields used in entity mapping.
 */
public interface CreditHourTrackingUpsertDto {
    /**
     * Gets teacher ID.
     * @return Teacher identifier
     */
    Long getTeacherId();

    /**
     * Gets academic year ID.
     * @return Academic year identifier
     */
    Long getAcademicYearId();

    /**
     * Gets assignments count.
     * @return Number of assignments
     */
    Integer getAssignmentsCount();

    /**
     * Gets credit hours allocated.
     * @return Credit hours allocated
     */
    Double getCreditHoursAllocated();

    /**
     * Gets credit balance.
     * @return Credit balance
     */
    Double getCreditBalance();

    /**
     * Gets notes.
     * @return Notes text
     */
    String getNotes();
}
