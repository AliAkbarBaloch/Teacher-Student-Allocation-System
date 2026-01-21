package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AcademicYear entity operations.
 */
@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long>, JpaSpecificationExecutor<AcademicYear> {
    /**
     * Find academic year by year name.
     * 
     * @param yearName the year name (e.g., "2025/2026")
     * @return optional containing the academic year if found
     */
    Optional<AcademicYear> findByYearName(String yearName);
}
