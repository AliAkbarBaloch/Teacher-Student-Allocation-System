package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for Teacher entity with support for dynamic queries.
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long>, JpaSpecificationExecutor<Teacher> {

    /**
     * Find teacher by email.
     *
     * @param email Teacher email
     * @return Optional containing the teacher if found
     */
    Optional<Teacher> findByEmail(String email);

    /**
     * Check if a teacher with the given email exists (excluding a specific ID for update operations).
     *
     * @param email Teacher email
     * @param id    Teacher ID to exclude
     * @return true if email exists for another teacher
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Teacher t WHERE t.email = :email AND t.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    /**
     * Check if email exists.
     *
     * @param email Teacher email
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Count active teachers by school ID.
     *
     * @param schoolId School ID
     * @param employmentStatus Teacher.EmploymentStatus
     * @return Count of teachers
     */
    long countBySchoolIdAndEmploymentStatus(Long schoolId, Teacher.EmploymentStatus employmentStatus);

    /**
     * Count teachers by employment status.
     *
     * @param employmentStatus Employment status
     * @return Count of teachers
     */
    long countByEmploymentStatus(EmploymentStatus employmentStatus);

    /**
     * Find all emails that exist in the database from the given set.
     * Used for batch duplicate email checking during bulk import.
     *
     * @param emails Set of email addresses to check
     * @return Set of emails that already exist in the database
     */
    @Query("SELECT t.email FROM Teacher t WHERE t.email IN :emails")
    Set<String> findExistingEmails(@Param("emails") Set<String> emails);

    List<Teacher> findAllByIsActiveTrue();
}
