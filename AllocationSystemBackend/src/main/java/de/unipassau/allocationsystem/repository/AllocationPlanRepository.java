package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing AllocationPlan entities.
 */
@Repository
public interface AllocationPlanRepository extends JpaRepository<AllocationPlan, Long>, 
                                                   JpaSpecificationExecutor<AllocationPlan> {

    /**
     * Check if an allocation plan exists for a given year and version.
     * Used for uniqueness validation.
     */
    boolean existsByAcademicYearIdAndPlanVersion(Long yearId, String planVersion);

    /**
     * Check if an allocation plan exists for a given year and version, excluding a specific plan ID.
     * Used for uniqueness validation during updates.
     */
    @Query("SELECT CASE WHEN COUNT(ap) > 0 THEN true ELSE false END FROM AllocationPlan ap " +
           "WHERE ap.academicYear.id = :yearId AND ap.planVersion = :planVersion AND ap.id <> :excludeId")
    boolean existsByYearAndVersionExcludingId(
        @Param("yearId") Long yearId,
        @Param("planVersion") String planVersion,
        @Param("excludeId") Long excludeId
    );

    /**
     * Find all allocation plans for a specific academic year.
     */
    List<AllocationPlan> findByAcademicYearId(Long yearId);

    /**
     * Find all allocation plans for a specific academic year with a specific status.
     */
    List<AllocationPlan> findByAcademicYearIdAndStatus(Long yearId, PlanStatus status);

    /**
     * Find the current allocation plan for a specific academic year.
     */
    Optional<AllocationPlan> findByAcademicYearIdAndIsCurrentTrue(Long yearId);

    /**
     * Find all current allocation plans (should ideally return at most one per year).
     */
    List<AllocationPlan> findByIsCurrentTrue();

    /**
     * Count allocation plans for a specific academic year.
     */
    long countByAcademicYearId(Long yearId);

    /**
     * Count allocation plans for a specific academic year and status.
     */
    long countByAcademicYearIdAndStatus(Long yearId, PlanStatus status);

    /**
     * Set is_current to false for all plans of a specific academic year.
     * This is used when setting a new plan as current.
     */
    @Modifying
    @Query("UPDATE AllocationPlan ap SET ap.isCurrent = false WHERE ap.academicYear.id = :yearId")
    void unsetCurrentForYear(@Param("yearId") Long yearId);

    /**
     * Set is_current to false for all plans of a specific academic year except the given plan ID.
     */
    @Modifying
    @Query("UPDATE AllocationPlan ap SET ap.isCurrent = false " +
           "WHERE ap.academicYear.id = :yearId AND ap.id <> :excludeId")
    void unsetCurrentForYearExcept(@Param("yearId") Long yearId, @Param("excludeId") Long excludeId);


    /**
     * Find allocation plans by academic year ID with optional filters.
     * This is a base query that can be enhanced with Specification for dynamic filtering.
     */
    @Query("SELECT ap FROM AllocationPlan ap " +
           "WHERE ap.academicYear.id = :yearId " +
           "AND (:status IS NULL OR ap.status = :status) " +
           "AND (:isCurrent IS NULL OR ap.isCurrent = :isCurrent)")
    List<AllocationPlan> findByYearIdWithFilters(
        @Param("yearId") Long yearId,
        @Param("status") PlanStatus status,
        @Param("isCurrent") Boolean isCurrent
    );
}
