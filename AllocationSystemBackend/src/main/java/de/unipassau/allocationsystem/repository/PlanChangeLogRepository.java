package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PlanChangeLogRepository extends JpaRepository<PlanChangeLog, Long> {

    Page<PlanChangeLog> findByAllocationPlanId(Long planId, Pageable pageable);

    Page<PlanChangeLog> findByUser(User user, Pageable pageable);

    Page<PlanChangeLog> findByEntityType(String entityType, Pageable pageable);

        Page<PlanChangeLog> findByChangeType(String changeType, Pageable pageable);

    Page<PlanChangeLog> findByEventTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT p FROM PlanChangeLog p WHERE " +
            "(:planId IS NULL OR (p.allocationPlan IS NOT NULL AND p.allocationPlan.id = :planId)) AND " +
            "(:userId IS NULL OR (p.user IS NOT NULL AND p.user.id = :userId)) AND " +
            "(:entityType IS NULL OR p.entityType = :entityType) AND " +
            "(:changeType IS NULL OR p.changeType = :changeType) AND " +
            "(:startDate IS NULL OR p.eventTimestamp >= :startDate) AND " +
            "(:endDate IS NULL OR p.eventTimestamp <= :endDate)")
    Page<PlanChangeLog> findByFilters(
            @Param("planId") Long planId,
            @Param("userId") Long userId,
            @Param("entityType") String entityType,
            @Param("changeType") String changeType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
