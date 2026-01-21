package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.dto.report.school.SchoolProfileDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for School entity operations.
 */
@Repository
public interface SchoolRepository extends JpaRepository<School, Long>, JpaSpecificationExecutor<School> {

    /**
     * Find school by name.
     */
    Optional<School> findBySchoolName(String schoolName);

    /**
     * Find schools by type.
     */
    List<School> findBySchoolType(SchoolType schoolType);

    /**
     * Find schools by zone number.
     */
    List<School> findByZoneNumber(Integer zoneNumber);

    /**
     * Find active schools.
     */
    List<School> findByIsActive(Boolean isActive);

    /**
     * Find schools by type and zone.
     */
    List<School> findBySchoolTypeAndZoneNumber(SchoolType schoolType, Integer zoneNumber);

    /**
     * Find active schools by type.
     */
    List<School> findBySchoolTypeAndIsActive(SchoolType schoolType, Boolean isActive);

    /**
     * Check if school name exists (for uniqueness validation).
     */
    boolean existsBySchoolName(String schoolName);

    /**
     * Check if school name exists excluding specific id (for update validation).
     */
    boolean existsBySchoolNameAndIdNot(String schoolName, Long id);

    @Query("SELECT new de.unipassau.allocationsystem.dto.report.school.SchoolProfileDto(" +
            "s.id, " +
            "s.schoolName, " +
            "s.schoolType, " + // This passes the ENUM
            "s.zoneNumber, " +
            "s.transportAccessibility, " +
            "s.isActive, " +
            "(SELECT COUNT(t) FROM Teacher t WHERE t.school = s), " + // This returns LONG
            "(SELECT COUNT(t) FROM Teacher t WHERE t.school = s AND t.employmentStatus = 'ACTIVE')" + // This returns LONG
            ") FROM School s")
    /**
     * Find all schools with profile data including teacher counts.
     * 
     * @return list of school profile DTOs
     */
    List<SchoolProfileDto> findAllSchoolProfiles();
}
