package de.unipassau.allocationsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.unipassau.allocationsystem.entity.TeacherAssignment;

/**
 * Repository for TeacherAssignment entity operations.
 * Manages teacher assignments to internship types and subjects.
 */
@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long>, JpaSpecificationExecutor<TeacherAssignment>  {

    /**
     * Check if assignment exists for the given plan, teacher, internship type, and subject.
     * 
     * @param planId the allocation plan ID
     * @param teacherId the teacher ID
     * @param internshipTypeId the internship type ID
     * @param subjectId the subject ID
     * @return true if assignment exists, false otherwise
     */
    boolean existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(Long planId, Long teacherId, Long internshipTypeId, Long subjectId);

    /**
     * Find assignments by allocation plan ID with pagination.
     * 
     * @param planId the allocation plan ID
     * @param pageable pagination parameters
     * @return page of teacher assignments
     */
    Page<TeacherAssignment> findByAllocationPlanId(Long planId, Pageable pageable);

    /**
     * Find assignments with optional filters.
     * 
     * @param planId the allocation plan ID
     * @param teacherId optional teacher ID filter
     * @param internshipTypeId optional internship type ID filter
     * @param subjectId optional subject ID filter
     * @param status optional assignment status filter
     * @param pageable pagination parameters
     * @return page of filtered teacher assignments
     */
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.allocationPlan.id = :planId " +
           "AND (:teacherId IS NULL OR ta.teacher.id = :teacherId) " +
           "AND (:internshipTypeId IS NULL OR ta.internshipType.id = :internshipTypeId) " +
           "AND (:subjectId IS NULL OR ta.subject.id = :subjectId) " +
           "AND (:status IS NULL OR ta.assignmentStatus = :status)")
    Page<TeacherAssignment> findByPlanWithFilters(@Param("planId") Long planId,
                                                  @Param("teacherId") Long teacherId,
                                                  @Param("internshipTypeId") Long internshipTypeId,
                                                  @Param("subjectId") Long subjectId,
                                                  @Param("status") TeacherAssignment.AssignmentStatus status,
                                                  Pageable pageable);

    /**
     * Find assignments for a teacher in a specific academic year.
     * 
     * @param teacherId the teacher ID
     * @param yearId the academic year ID
     * @return list of teacher assignments
     */
    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacher.id = :teacherId AND ta.allocationPlan.academicYear.id = :yearId")
    List<TeacherAssignment> findByTeacherIdAndYearId(@Param("teacherId") Long teacherId, @Param("yearId") Long yearId);


    /**
     * Find all assignments for a plan with related entities eagerly loaded.
     * 
     * @param planId the allocation plan ID
     * @return list of teacher assignments with fetched relationships
     */
    @Query("SELECT ta FROM TeacherAssignment ta " +
            "JOIN FETCH ta.teacher t " +
            "JOIN FETCH t.school s " +
            "JOIN FETCH ta.internshipType it " +
            "JOIN FETCH ta.subject sub " +
            "WHERE ta.allocationPlan.id = :planId")
    List<TeacherAssignment> findAllByPlanIdWithDetails(@Param("planId") Long planId);

    /**
     * Count assignments grouped by school type for a specific plan.
     * 
     * @param planId the allocation plan ID
     * @return list of arrays containing school type and count
     */
    @Query("SELECT s.schoolType, COUNT(ta) FROM TeacherAssignment ta " +
            "JOIN ta.teacher t JOIN t.school s " +
            "WHERE ta.allocationPlan.id = :planId " +
            "GROUP BY s.schoolType")
    List<Object[]> countAssignmentsBySchoolType(@Param("planId") Long planId);

    /**
     * Get all assignments for an allocation plan.
     * 
     * @param planId the allocation plan ID
     * @return list of teacher assignments
     */
    List<TeacherAssignment> findByAllocationPlanId(Long planId);

    /**
     * Find assignment by the 4-part composite key.
     * 
     * @param planId the allocation plan ID
     * @param teacherId the teacher ID
     * @param internshipTypeId the internship type ID
     * @param subjectId the subject ID
     * @return Optional containing the assignment if found
     */
    java.util.Optional<TeacherAssignment> findByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(
        Long planId, Long teacherId, Long internshipTypeId, Long subjectId);
}
