package de.unipassau.allocationsystem.dto.internshiptype;

/**
 * Common interface for internship type create and update DTOs.
 * Provides shared getters for common fields used in entity mapping.
 */
public interface InternshipTypeUpsertDto {
    /**
     * Gets internship code.
     * @return Internship code
     */
    String getInternshipCode();

    /**
     * Gets full name.
     * @return Full name
     */
    String getFullName();

    /**
     * Gets timing.
     * @return Timing information
     */
    String getTiming();

    /**
     * Gets period type.
     * @return Period type
     */
    String getPeriodType();

    /**
     * Gets semester.
     * @return Semester number
     */
    Integer getSemester();

    /**
     * Gets subject specific flag.
     * @return Whether internship is subject specific
     */
    Boolean getIsSubjectSpecific();

    /**
     * Gets priority order.
     * @return Priority order
     */
    Integer getPriorityOrder();
}
