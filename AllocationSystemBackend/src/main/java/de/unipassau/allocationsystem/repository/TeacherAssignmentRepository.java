package de.unipassau.allocationsystem.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.unipassau.allocationsystem.entity.TeacherAssignment;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long>, JpaSpecificationExecutor<TeacherAssignment>  {

    boolean existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(Long planId, Long teacherId, Long internshipTypeId, Long subjectId);

    Page<TeacherAssignment> findByAllocationPlanId(Long planId, Pageable pageable);

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

    @Query("SELECT ta FROM TeacherAssignment ta WHERE ta.teacher.id = :teacherId AND ta.allocationPlan.academicYear.id = :yearId")
    List<TeacherAssignment> findByTeacherIdAndYearId(@Param("teacherId") Long teacherId, @Param("yearId") Long yearId);


    @Query("SELECT ta FROM TeacherAssignment ta " +
            "JOIN FETCH ta.teacher t " +
            "JOIN FETCH t.school s " +
            "JOIN FETCH ta.internshipType it " +
            "JOIN FETCH ta.subject sub " +
            "WHERE ta.allocationPlan.id = :planId")
    List<TeacherAssignment> findAllByPlanIdWithDetails(@Param("planId") Long planId);

    // Count assignments for a specific plan, grouped by school type
    @Query("SELECT s.schoolType, COUNT(ta) FROM TeacherAssignment ta " +
            "JOIN ta.teacher t JOIN t.school s " +
            "WHERE ta.allocationPlan.id = :planId " +
            "GROUP BY s.schoolType")
    List<Object[]> countAssignmentsBySchoolType(@Param("planId") Long planId);

    // Get all assignments for a plan
    List<TeacherAssignment> findByAllocationPlanId(Long planId);
}
