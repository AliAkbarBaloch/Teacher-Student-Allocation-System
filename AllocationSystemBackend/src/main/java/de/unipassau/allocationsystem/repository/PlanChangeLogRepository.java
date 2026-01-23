package de.unipassau.allocationsystem.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.unipassau.allocationsystem.entity.PlanChangeLog;

/**
 * Repository for PlanChangeLog entity operations.
 * Tracks changes to allocation plans.
 */
@Repository
public interface PlanChangeLogRepository extends JpaRepository<PlanChangeLog, Long>, JpaSpecificationExecutor<PlanChangeLog> {

    /**
     * Find change logs by allocation plan ID with pagination.
     * 
     * @param planId the allocation plan ID
     * @param pageable pagination parameters
     * @return page of change logs
     */
    Page<PlanChangeLog> findByAllocationPlanId(Long planId, Pageable pageable);

    /**
     * Find change logs by entity type with pagination.
     * 
     * @param entityType the entity type
     * @param pageable pagination parameters
     * @return page of change logs
     */
    Page<PlanChangeLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find change logs by change type with pagination.
     * 
     * @param changeType the change type
     * @param pageable pagination parameters
     * @return page of change logs
     */
    Page<PlanChangeLog> findByChangeType(String changeType, Pageable pageable);

    /**
     * Find change logs created between start and end dates with pagination.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination parameters
     * @return page of change logs
     */
    Page<PlanChangeLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find change logs with multiple optional filters.
     * 
     * @param planId optional allocation plan ID filter
     * @param entityType optional entity type filter
     * @param changeType optional change type filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param pageable pagination parameters
     * @return page of filtered change logs
     */
    @Query("SELECT p FROM PlanChangeLog p WHERE " +
            "(:planId IS NULL OR (p.allocationPlan IS NOT NULL AND p.allocationPlan.id = :planId)) AND " +
            "(:entityType IS NULL OR p.entityType = :entityType) AND " +
            "(:changeType IS NULL OR p.changeType = :changeType) AND " +
            "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR p.createdAt <= :endDate)")
    Page<PlanChangeLog> findByFilters(
            @Param("planId") Long planId,
            @Param("entityType") String entityType,
            @Param("changeType") String changeType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
