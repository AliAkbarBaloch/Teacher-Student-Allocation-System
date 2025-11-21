package de.unipassau.allocationsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.PlanChangeLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanChangeLogService {

    /**
     * Note: This service intentionally does NOT use the `@Audited` annotation.
     *
     * Reason: `PlanChangeLog` entries themselves are an audit-style record
     * of changes to allocation plans. Annotating methods that create
     * `PlanChangeLog` entries with `@Audited` would cause the audit aspect
     * to generate additional audit log entries for those operations, which
     * can produce duplicate/a cascading audit behavior. To avoid recursion
     * and noisy duplicate audit records, we keep plan-change logging
     * separate from the application's `AuditLog` mechanism.
     */

    private final PlanChangeLogRepository planChangeLogRepository;
    private final UserRepository userRepository;
    private final AllocationPlanRepository allocationPlanRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new plan change log entry. Intended to be called by other services.
     */
    @Transactional
    public PlanChangeLog logPlanChange(Long planId,
                                          Long userId,
                                          String changeType,
                                          String entityType,
                                          Long entityId,
                                          Object oldValue,
                                          Object newValue,
                                          String reason) {

        AllocationPlan plan = allocationPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + planId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String oldJson = serialize(oldValue);
        String newJson = serialize(newValue);

        PlanChangeLog log = PlanChangeLog.builder()
                .allocationPlan(plan)
                .user(user)
            .changeType(changeType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldJson)
                .newValue(newJson)
                .reason(reason)
                .eventTimestamp(LocalDateTime.now())
                .build();

        PlanChangeLog saved = planChangeLogRepository.save(log);
        return saved;
    }

    private String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object for plan change log", e);
            return obj.toString();
        }
    }

    public Page<PlanChangeLog> getLogsByPlan(Long planId, Long userId, String entityType, String changeType,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (planId != null && !allocationPlanRepository.existsById(planId)) {
            throw new ResourceNotFoundException("Allocation plan not found with id: " + planId);
        }
        return planChangeLogRepository.findByFilters(planId, userId, entityType, changeType, startDate, endDate, pageable);
    }

    public Page<PlanChangeLog> getLogs(Long planId, Long userId, String entityType, String changeType,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return planChangeLogRepository.findByFilters(planId, userId, entityType, changeType, startDate, endDate, pageable);
    }
}
