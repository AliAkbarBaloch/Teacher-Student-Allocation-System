package de.unipassau.allocationsystem.dto.teacher;

import de.unipassau.allocationsystem.entity.Teacher;

/**
 * Upsert interface for Teacher DTO operations.
 * Defines common getter methods for both TeacherCreateDto and TeacherUpdateDto.
 */
public interface TeacherUpsertDto {
    /**
     * Gets the teacher's first name.
     * @return first name
     */
    String getFirstName();

    /**
     * Gets the teacher's last name.
     * @return last name
     */
    String getLastName();

    /**
     * Gets the teacher's email.
     * @return email
     */
    String getEmail();

    /**
     * Gets the teacher's phone.
     * @return phone
     */
    String getPhone();

    /**
     * Gets whether teacher is part-time.
     * @return part-time status
     */
    Boolean getIsPartTime();

    /**
     * Gets working hours per week.
     * @return working hours
     */
    Integer getWorkingHoursPerWeek();

    /**
     * Gets employment status.
     * @return employment status
     */
    Teacher.EmploymentStatus getEmploymentStatus();

    /**
     * Gets usage cycle.
     * @return usage cycle
     */
    Teacher.UsageCycle getUsageCycle();
}
