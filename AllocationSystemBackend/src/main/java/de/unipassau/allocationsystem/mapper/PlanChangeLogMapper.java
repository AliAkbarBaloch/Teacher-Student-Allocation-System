package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogCreateDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogUpdateDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogResponseDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanChangeLogMapper implements BaseMapper<PlanChangeLog, PlanChangeLogCreateDto, PlanChangeLogUpdateDto, PlanChangeLogResponseDto> {

    private final AllocationPlanRepository allocationPlanRepository;

    @Override
    public PlanChangeLog toEntityCreate(PlanChangeLogCreateDto dto) {
        if (dto == null) {
            return null;
        }
        PlanChangeLog entity = new PlanChangeLog();
        if (dto.getPlanId() != null) {
            AllocationPlan plan = allocationPlanRepository.findById(dto.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + dto.getPlanId()));
            entity.setAllocationPlan(plan);
        }
        entity.setChangeType(dto.getChangeType());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityId(dto.getEntityId());
        entity.setOldValue(dto.getOldValue());
        entity.setNewValue(dto.getNewValue());
        entity.setReason(dto.getReason());
        return entity;
    }

    @Override
    public PlanChangeLog toEntityUpdate(PlanChangeLogUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        PlanChangeLog entity = new PlanChangeLog();
        entity.setChangeType(dto.getChangeType());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityId(dto.getEntityId());
        entity.setOldValue(dto.getOldValue());
        entity.setNewValue(dto.getNewValue());
        entity.setReason(dto.getReason());
        return entity;
    }

    @Override
    public PlanChangeLogResponseDto toResponseDto(PlanChangeLog entity) {
        if (entity == null) {
            return null;
        }
        return new PlanChangeLogResponseDto(
                entity.getId(),
                entity.getAllocationPlan() != null ? entity.getAllocationPlan().getId() : null,
                entity.getChangeType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<PlanChangeLogResponseDto> toResponseDtoList(List<PlanChangeLog> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(PlanChangeLogUpdateDto dto, PlanChangeLog entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getChangeType() != null) {
            entity.setChangeType(dto.getChangeType());
        }
        if (dto.getEntityType() != null) {
            entity.setEntityType(dto.getEntityType());
        }
        if (dto.getEntityId() != null) {
            entity.setEntityId(dto.getEntityId());
        }
        if (dto.getOldValue() != null) {
            entity.setOldValue(dto.getOldValue());
        }
        if (dto.getNewValue() != null) {
            entity.setNewValue(dto.getNewValue());
        }
        if (dto.getReason() != null) {
            entity.setReason(dto.getReason());
        }
    }
}