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

@Repository
public interface PlanChangeLogRepository extends JpaRepository<PlanChangeLog, Long>, JpaSpecificationExecutor<PlanChangeLog> {

    Page<PlanChangeLog> findByAllocationPlanId(Long planId, Pageable pageable);

    Page<PlanChangeLog> findByEntityType(String entityType, Pageable pageable);

    Page<PlanChangeLog> findByChangeType(String changeType, Pageable pageable);

    Page<PlanChangeLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

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
