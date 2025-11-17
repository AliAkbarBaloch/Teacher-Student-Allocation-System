package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
