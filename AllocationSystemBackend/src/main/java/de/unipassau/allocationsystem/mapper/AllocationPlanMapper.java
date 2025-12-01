package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AllocationPlan entities and DTOs.
 */
@Component
public class AllocationPlanMapper {

    /**
     * Convert AllocationPlan entity to response DTO.
     *
     * @param allocationPlan AllocationPlan entity
     * @return AllocationPlanResponseDto
     */
    public AllocationPlanResponseDto toResponseDto(AllocationPlan allocationPlan) {
        if (allocationPlan == null) {
            return null;
        }

        return AllocationPlanResponseDto.builder()
                .id(allocationPlan.getId())
                .yearId(allocationPlan.getAcademicYear().getId())
                .yearName(allocationPlan.getAcademicYear().getYearName())
                .planName(allocationPlan.getPlanName())
                .planVersion(allocationPlan.getPlanVersion())
                .status(allocationPlan.getStatus())
                .statusDisplayName(allocationPlan.getStatus().getDisplayName())
                .createdByUserId(allocationPlan.getCreatedByUser().getId())
                .createdByUserName(allocationPlan.getCreatedByUser().getFullName())
                .createdByUserEmail(allocationPlan.getCreatedByUser().getEmail())
                .isCurrent(allocationPlan.getIsCurrent())
                .notes(allocationPlan.getNotes())
                .createdAt(allocationPlan.getCreatedAt())
                .updatedAt(allocationPlan.getUpdatedAt())
                .build();
    }

    /**
     * Update AllocationPlan entity from update DTO.
     * Only updates non-null fields from the DTO.
     *
     * @param allocationPlan Existing AllocationPlan entity
     * @param dto            Update DTO
     */
    public void updateEntityFromDto(AllocationPlan allocationPlan, AllocationPlanUpdateDto dto) {
        if (dto.getPlanName() != null) {
            allocationPlan.setPlanName(dto.getPlanName());
        }
        if (dto.getStatus() != null) {
            allocationPlan.setStatus(dto.getStatus());
        }
        if (dto.getIsCurrent() != null) {
            allocationPlan.setIsCurrent(dto.getIsCurrent());
        }
        if (dto.getNotes() != null) {
            allocationPlan.setNotes(dto.getNotes());
        }
    }
}
