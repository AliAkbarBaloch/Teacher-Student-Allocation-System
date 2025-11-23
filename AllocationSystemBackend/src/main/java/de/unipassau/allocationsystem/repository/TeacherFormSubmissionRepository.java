package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for TeacherFormSubmission entity.
 */
@Repository
public interface TeacherFormSubmissionRepository extends JpaRepository<TeacherFormSubmission, Long>,
        JpaSpecificationExecutor<TeacherFormSubmission> {

    /**
     * Check if a form token already exists.
     *
     * @param formToken The form token to check
     * @return true if exists, false otherwise
     */
    boolean existsByFormToken(String formToken);

    /**
     * Find submission by form token.
     *
     * @param formToken The form token
     * @return Optional containing the submission if found
     */
    Optional<TeacherFormSubmission> findByFormToken(String formToken);

    /**
     * Check if a submission exists for a specific teacher and academic year combination.
     * This allows the same teacher to be invited for different academic years.
     * Only blocks duplicate submissions for the SAME teacher AND SAME year.
     *
     * @param teacherId The teacher ID
     * @param yearId The academic year ID
     * @return true if a submission exists for this teacher/year combination
     */
    @Query("SELECT CASE WHEN COUNT(tfs) > 0 THEN true ELSE false END FROM TeacherFormSubmission tfs " +
           "WHERE tfs.teacher.id = :teacherId AND tfs.academicYear.id = :yearId")
    boolean existsByTeacherIdAndAcademicYearId(@Param("teacherId") Long teacherId, @Param("yearId") Long yearId);
}
