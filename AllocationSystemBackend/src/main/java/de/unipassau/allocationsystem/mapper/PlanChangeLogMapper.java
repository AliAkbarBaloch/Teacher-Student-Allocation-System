package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanChangeLogMapper {

    private final UserRepository userRepository;
    private final AllocationPlanRepository allocationPlanRepository;

        public PlanChangeLog toEntity(PlanChangeLogDto dto) {
        if (dto == null) {
            return null;
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        AllocationPlan plan = allocationPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + dto.getPlanId()));

        PlanChangeLog.PlanChangeLogBuilder builder = PlanChangeLog.builder()
                .allocationPlan(plan)
                .user(user)
            .changeType(dto.getChangeType())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .oldValue(dto.getOldValue())
                .newValue(dto.getNewValue())
                .eventTimestamp(dto.getEventTimestamp())
                .reason(dto.getReason());

        if (dto.getId() != null && dto.getId() > 0) {
            builder.id(dto.getId());
        }

        return builder.build();
    }

    public PlanChangeLogDto toDto(PlanChangeLog entity) {
        if (entity == null) {
            return null;
        }

        return PlanChangeLogDto.builder()
                .id(entity.getId())
                .planId(entity.getAllocationPlan() != null ? entity.getAllocationPlan().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .changeType(entity.getChangeType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .eventTimestamp(entity.getEventTimestamp())
                .reason(entity.getReason())
                .build();
    }

    public List<PlanChangeLogDto> toDtoList(List<PlanChangeLog> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
