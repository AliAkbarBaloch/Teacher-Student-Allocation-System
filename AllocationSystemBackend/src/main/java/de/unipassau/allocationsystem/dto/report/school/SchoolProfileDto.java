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
     * Specific Constructor for JPQL Queries.
     * The argument types must MATCH the query output exactly.
     * * @param schoolId - s.id (Long)
     * @param schoolName - s.schoolName (String)
     * @param schoolType - s.schoolType (Enum: School.SchoolType)
     * @param schoolId - s.id (Long)
     * @param schoolName - s.name (String)
     * @param schoolType - s.schoolType (School.SchoolType)
     * @param zoneNumber - s.zoneNumber (Integer)
     * @param transportAccessibility - s.transportAccessibility (String)
     * @param isActive - s.isActive (Boolean)
     * @param totalTeachers - COUNT(t) (Long)
     * @param activeTeachers - COUNT(t) (Long)
     */
    @java.beans.ConstructorProperties({"schoolId", "schoolName", "schoolType", "zoneNumber", 
                                       "transportAccessibility", "isActive", "totalTeachers", "activeTeachers"})
    public SchoolProfileDto(Long schoolId,
                            String schoolName,
                            School.SchoolType schoolType,
                            Integer zoneNumber,
                            String transportAccessibility,
                            Boolean isActive,
                            Long totalTeachers,
                            Long activeTeachers) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        // Convert Enum to String for the frontend
        this.schoolType = schoolType != null ? schoolType.name() : null;
        this.zoneNumber = zoneNumber;
        this.transportAccessibility = transportAccessibility;
        // Handle potential nulls from DB
        this.isActive = isActive != null && isActive;
        this.totalTeachers = totalTeachers != null ? totalTeachers : 0L;
        this.activeTeachers = activeTeachers != null ? activeTeachers : 0L;
    }
}
