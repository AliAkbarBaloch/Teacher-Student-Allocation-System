package de.unipassau.allocationsystem.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.PlanChangeLogRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanChangeLogService implements CrudService<PlanChangeLog, Long> {

    private final PlanChangeLogRepository planChangeLogRepository;
    private final AllocationPlanRepository allocationPlanRepository;
    private final ObjectMapper objectMapper;

    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "planId", "changeType", "entityType", "entityId", "createdAt", "updatedAt");
    }

    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return planChangeLogRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        // No searchValue logic for PlanChangeLog, but you can add if needed
        Page<PlanChangeLog> page = planChangeLogRepository.findAll(pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PlanChangeLog> getAll() {
        return planChangeLogRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PlanChangeLog> getById(Long id) {
        return planChangeLogRepository.findById(id);
    }

    @Transactional
    @Override
    public PlanChangeLog create(PlanChangeLog entity) {
        // No uniqueness constraint for PlanChangeLog
        return planChangeLogRepository.save(entity);
    }

    @Transactional
    @Override
    public PlanChangeLog update(Long id, PlanChangeLog data) {
        PlanChangeLog existing = planChangeLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlanChangeLog not found with id: " + id));

        if (data.getChangeType() != null) {
            existing.setChangeType(data.getChangeType());
        }
        if (data.getEntityType() != null) {
            existing.setEntityType(data.getEntityType());
        }
        if (data.getEntityId() != null) {
            existing.setEntityId(data.getEntityId());
        }
        if (data.getOldValue() != null) {
            existing.setOldValue(data.getOldValue());
        }
        if (data.getNewValue() != null) {
            existing.setNewValue(data.getNewValue());
        }
        if (data.getReason() != null) {
            existing.setReason(data.getReason());
        }

        return planChangeLogRepository.save(existing);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!planChangeLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("PlanChangeLog not found with id: " + id);
        }
        planChangeLogRepository.deleteById(id);
    }

    /**
     * Create a new plan change log entry. Intended to be called by other services.
     */
    @Transactional
    public PlanChangeLog logPlanChange(Long planId, String changeType, String entityType, Long entityId, Object oldValue, Object newValue, String reason) {

        AllocationPlan plan = allocationPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + planId));

        String oldJson = serialize(oldValue);
        String newJson = serialize(newValue);

        PlanChangeLog log = PlanChangeLog.builder()
                .allocationPlan(plan)
                .eventTimestamp(LocalDateTime.now())
            .changeType(changeType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldJson)
                .newValue(newJson)
                .reason(reason)
                .createdAt(LocalDateTime.now())
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

    public Page<PlanChangeLog> getLogsByPlan(Long planId, String entityType, String changeType,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (planId != null && !allocationPlanRepository.existsById(planId)) {
            throw new ResourceNotFoundException("Allocation plan not found with id: " + planId);
        }
        return planChangeLogRepository.findByFilters(planId, entityType, changeType, startDate, endDate, pageable);
    }

    public Page<PlanChangeLog> getLogs(Long planId, String entityType, String changeType,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return planChangeLogRepository.findByFilters(planId, entityType, changeType, startDate, endDate, pageable);
    }
}
