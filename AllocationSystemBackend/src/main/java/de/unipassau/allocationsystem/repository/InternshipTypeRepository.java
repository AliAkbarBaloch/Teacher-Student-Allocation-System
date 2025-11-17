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
     * @param internshipCode Internship code
     * @return Optional internship type
     */
    Optional<InternshipType> findByInternshipCode(String internshipCode);

    /**
     * Check if internship code exists.
     *
     * @param internshipCode Internship code
     * @return true if exists
     */
    boolean existsByInternshipCode(String internshipCode);
}
