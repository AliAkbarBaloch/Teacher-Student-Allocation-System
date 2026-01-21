package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.CreditHourTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CreditHourTracking entity operations.
 * Tracks teacher credit hours per academic year.
 */
public interface CreditHourTrackingRepository extends JpaRepository<CreditHourTracking, Long>, JpaSpecificationExecutor<CreditHourTracking> {
    /**
     * Find credit hour tracking by teacher and academic year.
     * 
     * @param teacherId the teacher ID
     * @param yearId the academic year ID
     * @return optional containing the tracking record if found
     */
    Optional<CreditHourTracking> findByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);
    
    /**
     * Find all credit hour tracking records for an academic year.
     * 
     * @param yearId the academic year ID
     * @return list of tracking records
     */
    List<CreditHourTracking> findByAcademicYearId(Long yearId);
    
    /**
     * Check if credit hour tracking exists for teacher and academic year.
     * 
     * @param teacherId the teacher ID
     * @param yearId the academic year ID
     * @return true if tracking exists, false otherwise
     */
    boolean existsByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);
}
