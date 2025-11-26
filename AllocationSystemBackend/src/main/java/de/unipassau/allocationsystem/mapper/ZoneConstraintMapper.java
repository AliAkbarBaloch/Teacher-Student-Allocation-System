package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ZoneConstraint entity and DTOs.
 */
@Component
public class ZoneConstraintMapper implements BaseMapper<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> {

    @Override
    public ZoneConstraint toEntityCreate(ZoneConstraintCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        ZoneConstraint zoneConstraint = new ZoneConstraint();
        zoneConstraint.setZoneNumber(createDto.getZoneNumber());
        // Note: internshipType needs to be set by service layer
        zoneConstraint.setIsAllowed(createDto.getIsAllowed());
        zoneConstraint.setDescription(createDto.getDescription());
        return zoneConstraint;
    }

    @Override
    public ZoneConstraint toEntityUpdate(ZoneConstraintUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        ZoneConstraint zoneConstraint = new ZoneConstraint();
        zoneConstraint.setZoneNumber(updateDto.getZoneNumber());
        // Note: internshipType needs to be set by service layer
        zoneConstraint.setIsAllowed(updateDto.getIsAllowed());
        zoneConstraint.setDescription(updateDto.getDescription());
        return zoneConstraint;
    }

    @Override
    public ZoneConstraintResponseDto toResponseDto(ZoneConstraint zoneConstraint) {
        if (zoneConstraint == null) {
            return null;
        }
        return new ZoneConstraintResponseDto(
                zoneConstraint.getId(),
                zoneConstraint.getZoneNumber(),
                zoneConstraint.getInternshipType() != null ? 
                        zoneConstraint.getInternshipType().getId() : null,
                zoneConstraint.getInternshipType() != null ? 
                        zoneConstraint.getInternshipType().getInternshipCode() : null,
                zoneConstraint.getInternshipType() != null ? 
                        zoneConstraint.getInternshipType().getFullName() : null,
                zoneConstraint.getIsAllowed(),
                zoneConstraint.getDescription(),
                zoneConstraint.getCreatedAt(),
                zoneConstraint.getUpdatedAt()
        );
    }

    @Override
    public List<ZoneConstraintResponseDto> toResponseDtoList(List<ZoneConstraint> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(ZoneConstraintUpdateDto updateDto, ZoneConstraint zoneConstraint) {
        if (updateDto == null || zoneConstraint == null) {
            return;
        }

        if (updateDto.getZoneNumber() != null) {
            zoneConstraint.setZoneNumber(updateDto.getZoneNumber());
        }
        
        // Note: internshipTypeId needs to be resolved to InternshipType entity by the service layer

        if (updateDto.getIsAllowed() != null) {
            zoneConstraint.setIsAllowed(updateDto.getIsAllowed());
        }

        if (updateDto.getDescription() != null) {
            zoneConstraint.setDescription(updateDto.getDescription());
        }
    }
}

