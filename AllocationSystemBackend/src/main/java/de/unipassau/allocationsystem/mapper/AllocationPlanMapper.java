package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AllocationPlanMapper implements BaseMapper<AllocationPlan, AllocationPlanCreateDto, AllocationPlanUpdateDto, AllocationPlanResponseDto> {

    @Override
    public AllocationPlan toEntityCreate(AllocationPlanCreateDto dto) {
        if (dto == null) {
            return null;
        }
        AllocationPlan entity = new AllocationPlan();
        entity.setPlanName(dto.getPlanName());
        entity.setPlanVersion(dto.getPlanVersion());
        entity.setStatus(dto.getStatus());
        entity.setIsCurrent(dto.getIsCurrent());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    @Override
    public AllocationPlan toEntityUpdate(AllocationPlanUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        AllocationPlan entity = new AllocationPlan();
        entity.setPlanName(dto.getPlanName());
        entity.setStatus(dto.getStatus());
        entity.setIsCurrent(dto.getIsCurrent());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    @Override
    public AllocationPlanResponseDto toResponseDto(AllocationPlan entity) {
        if (entity == null) {
            return null;
        }
        Long yearId = null;
        String yearName = null;
        if (entity.getAcademicYear() != null) {
            yearId = entity.getAcademicYear().getId();
            yearName = entity.getAcademicYear().getYearName();
        }
        String statusDisplayName = entity.getStatus() != null ? entity.getStatus().getDisplayName() : null;

        return new AllocationPlanResponseDto(
                entity.getId(),
                yearId,
                yearName,
                entity.getPlanName(),
                entity.getPlanVersion(),
                entity.getStatus(),
                statusDisplayName,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIsCurrent(),
                entity.getNotes()
        );
    }

    @Override
    public List<AllocationPlanResponseDto> toResponseDtoList(List<AllocationPlan> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(AllocationPlanUpdateDto dto, AllocationPlan entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getPlanName() != null) {
            entity.setPlanName(dto.getPlanName());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getIsCurrent() != null) {
            entity.setIsCurrent(dto.getIsCurrent());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}
