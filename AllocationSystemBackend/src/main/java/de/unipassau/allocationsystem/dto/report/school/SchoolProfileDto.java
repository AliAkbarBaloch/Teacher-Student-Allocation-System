package de.unipassau.allocationsystem.dto.report.school;

import de.unipassau.allocationsystem.entity.School;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed school profile information.
 * Includes school identification, location, accessibility, and associated teacher statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolProfileDto {
    private Long schoolId;
    private String schoolName;
    private String schoolType; // Stored as String for JSON
    private Integer zoneNumber;
    private String transportAccessibility;
    private boolean isActive;

    // Teacher Stats
    private Long totalTeachers;
    private Long activeTeachers;

    /**
     * Factory method for creating SchoolProfileDto from JPQL query results.
     * Groups school information and teacher statistics separately to reduce parameter count.
     * 
     * @param schoolId School identifier
     * @param schoolName Name of the school
     * @param schoolType School type enumeration
     * @param zoneNumber Zone number
     * @param transportAccessibility Transport accessibility description
     * @param isActive Active status flag
     * @param teacherStats Teacher statistics wrapper containing total and active counts
     * @return Constructed SchoolProfileDto instance
     */
    public static SchoolProfileDto fromQuery(Long schoolId,
                                             String schoolName,
                                             School.SchoolType schoolType,
                                             Integer zoneNumber,
                                             String transportAccessibility,
                                             Boolean isActive,
                                             TeacherStats teacherStats) {
        return SchoolProfileDto.builder()
                .schoolId(schoolId)
                .schoolName(schoolName)
                .schoolType(schoolType != null ? schoolType.name() : null)
                .zoneNumber(zoneNumber)
                .transportAccessibility(transportAccessibility)
                .isActive(isActive != null && isActive)
                .totalTeachers(teacherStats != null ? teacherStats.totalTeachers() : 0L)
                .activeTeachers(teacherStats != null ? teacherStats.activeTeachers() : 0L)
                .build();
    }

    /**
     * Wrapper for teacher statistics to reduce parameter count.
     * 
     * @param totalTeachers Total teacher count
     * @param activeTeachers Active teacher count
     */
    public record TeacherStats(Long totalTeachers, Long activeTeachers) {}
}
