package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Subject entity.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    /**
     * Find subject by subject code.
     * 
     * @param subjectCode the subject code
     * @return optional containing the subject if found
     */
    Optional<Subject> findBySubjectCode(String subjectCode);
    
    /**
     * Check if subject with the given code exists.
     * 
     * @param subjectCode the subject code
     * @return true if code exists, false otherwise
     */
    boolean existsBySubjectCode(String subjectCode);
}

