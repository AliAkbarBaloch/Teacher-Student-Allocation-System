package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreditHourTrackingMapper implements BaseMapper<CreditHourTracking, Object, CreditHourTrackingUpdateDto, CreditHourTrackingResponseDto> {

    @Override
    public CreditHourTracking toEntityCreate(Object createDto) {
        return null;
    }

    @Override
    public CreditHourTracking toEntityUpdate(CreditHourTrackingUpdateDto updateDto) {
        return null;
    }

    @Override
    public CreditHourTrackingResponseDto toResponseDto(CreditHourTracking entity) {
        if (entity == null) {
            return null;
        }
        CreditHourTrackingResponseDto dto = new CreditHourTrackingResponseDto();
        dto.setId(entity.getId());
        if (entity.getTeacher() != null) {
            dto.setTeacherId(entity.getTeacher().getId());
        }
        if (entity.getAcademicYear() != null) {
            dto.setYearId(entity.getAcademicYear().getId());
        }
        dto.setAssignmentsCount(entity.getAssignmentsCount());
        dto.setCreditHoursAllocated(entity.getCreditHoursAllocated());
        dto.setCreditBalance(entity.getCreditBalance());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    public List<CreditHourTrackingResponseDto> toResponseDtoList(List<CreditHourTracking> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(CreditHourTrackingUpdateDto updateDto, CreditHourTracking entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        if (updateDto.getNotes() != null) {
            entity.setNotes(updateDto.getNotes());
        }
        if (updateDto.getCreditHoursAllocated() != null) {
            entity.setCreditHoursAllocated(updateDto.getCreditHoursAllocated());
        }
        if (updateDto.getCreditBalance() != null) {
            entity.setCreditBalance(updateDto.getCreditBalance());
        }
    }
}
