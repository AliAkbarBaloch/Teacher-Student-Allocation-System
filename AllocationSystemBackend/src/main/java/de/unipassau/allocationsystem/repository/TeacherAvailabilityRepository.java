package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TeacherAvailability entity with custom query methods.
 */
@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long>, JpaSpecificationExecutor<TeacherAvailability> {

    /**
     * Check if availability entry exists for a specific teacher-year-internship combination.
     *
     * @param teacherId Teacher ID
     * @param yearId Year ID
     * @param internshipTypeId Internship type ID
     * @return true if exists
     */
    boolean existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(Long teacherId, Long yearId, Long internshipTypeId);

    /**
     * Check if availability entry exists excluding a specific ID.
     * Useful for update operations.
     *
     * @param teacherId Teacher ID
     * @param yearId Year ID
     * @param internshipTypeId Internship type ID
     * @param availabilityId ID to exclude
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END FROM TeacherAvailability ta " +
           "WHERE ta.teacher.id = :teacherId AND ta.academicYear.id = :yearId " +
           "AND ta.internshipType.id = :internshipTypeId AND ta.availabilityId != :availabilityId")
    boolean existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(
            @Param("teacherId") Long teacherId,
            @Param("yearId") Long yearId,
            @Param("internshipTypeId") Long internshipTypeId,
            @Param("availabilityId") Long availabilityId);

    /**
     * Find all availability entries for a specific teacher and year.
     *
     * @param teacherId Teacher ID
     * @param yearId Year ID
     * @return List of availability entries
     */
    List<TeacherAvailability> findByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);

    /**
     * Find all availability entries for a specific teacher.
     *
     * @param teacherId Teacher ID
     * @return List of availability entries
     */
    List<TeacherAvailability> findByTeacherId(Long teacherId);

    /**
     * Find availability entry by ID and teacher ID.
     * Ensures the entry belongs to the specified teacher.
     *
     * @param availabilityId Availability ID
     * @param teacherId Teacher ID
     * @return Optional availability entry
     */
    Optional<TeacherAvailability> findByAvailabilityIdAndTeacherId(Long availabilityId, Long teacherId);

    /**
     * Count availability entries by teacher ID and year ID.
     *
     * @param teacherId Teacher ID
     * @param yearId Year ID
     * @return Count of entries
     */
    long countByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);
}
