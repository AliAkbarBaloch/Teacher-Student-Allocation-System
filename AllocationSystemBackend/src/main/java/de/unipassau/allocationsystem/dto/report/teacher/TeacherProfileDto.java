package de.unipassau.allocationsystem.dto.report.teacher;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for detailed teacher profile information.
 * Includes personal details, employment status, qualifications, and availability.
 */
@Data
@Builder
public class TeacherProfileDto {
    private Long teacherId;
    private String fullName;
    private String email;
    private String schoolName;
    private String schoolType; // Primary vs Middle

    // Employment Data
    private String employmentStatus;
    private boolean isPartTime;
    private Integer workingHours;

    // Qualifications (e.g., ["Math", "German", "History"])
    private List<String> qualifiedSubjects;

    // Availability for the requested Academic Year
    private String availabilityStatusForYear; // "AVAILABLE", "PREFERRED", "NOT_SET"
    private String availabilityNotes;
}
