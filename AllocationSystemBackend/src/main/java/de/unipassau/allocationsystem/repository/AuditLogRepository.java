package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing AuditLog entities.
 * Provides methods for querying and filtering audit logs by various criteria.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user.
     */
    Page<AuditLog> findByUser(User user, Pageable pageable);

    /**
     * Find all audit logs for a specific user by user identifier.
     */
    Page<AuditLog> findByUserIdentifier(String userIdentifier, Pageable pageable);

    /**
     * Find all audit logs for a specific action type.
     */
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    /**
     * Find all audit logs for a specific target entity.
     */
    Page<AuditLog> findByTargetEntity(String targetEntity, Pageable pageable);

    /**
     * Find all audit logs for a specific target entity and record ID.
     */
    Page<AuditLog> findByTargetEntityAndTargetRecordId(
        String targetEntity, 
        String targetRecordId, 
        Pageable pageable
    );

    /**
     * Find audit logs within a date range.
     */
    Page<AuditLog> findByEventTimestampBetween(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );

    /**
     * Complex query combining multiple filters.
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR (a.user IS NOT NULL AND a.user.id = :userId)) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:targetEntity IS NULL OR a.targetEntity = :targetEntity) AND " +
           "(:startDate IS NULL OR a.eventTimestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.eventTimestamp <= :endDate)")
    Page<AuditLog> findByFilters(
        @Param("userId") Long userId,
        @Param("action") AuditAction action,
        @Param("targetEntity") String targetEntity,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Get audit log count by action type for reporting.
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
           "WHERE a.eventTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.action")
    List<Object[]> getActionStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get audit log count by entity type for reporting.
     */
    @Query("SELECT a.targetEntity, COUNT(a) FROM AuditLog a " +
           "WHERE a.eventTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.targetEntity")
    List<Object[]> getEntityStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get audit log count by user for reporting.
     */
    @Query("SELECT a.userIdentifier, COUNT(a) FROM AuditLog a " +
           "WHERE a.eventTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY a.userIdentifier " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getUserActivityStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find recent audit logs (for dashboard/monitoring).
     */
    List<AuditLog> findTop100ByOrderByEventTimestampDesc();

    /**
     * Count audit logs for a specific user within a time range.
     */
    long countByUserAndEventTimestampBetween(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
