package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.InternshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for InternshipType entity.
 */
@Repository
public interface InternshipTypeRepository extends JpaRepository<InternshipType, Long>, JpaSpecificationExecutor<InternshipType> {
    /**
     * Find internship type by code.
     * 
     * @param internshipCode the internship code
     * @return optional containing the internship type if found
     */
    Optional<InternshipType> findByInternshipCode(String internshipCode);
    
    /**
     * Check if internship type with the given code exists.
     * 
     * @param internshipCode the internship code
     * @return true if code exists, false otherwise
     */
    boolean existsByInternshipCode(String internshipCode);
}
