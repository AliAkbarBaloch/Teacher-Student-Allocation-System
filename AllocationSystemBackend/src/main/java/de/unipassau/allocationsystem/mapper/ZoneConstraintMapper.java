package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ZoneConstraint entity and DTOs.
 */
@Component
@RequiredArgsConstructor
public class ZoneConstraintMapper implements BaseMapper<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> {

    private final InternshipTypeRepository internshipTypeRepository;

    @Override
    public ZoneConstraint toEntityCreate(ZoneConstraintCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        ZoneConstraint zoneConstraint = new ZoneConstraint();
        zoneConstraint.setZoneNumber(createDto.getZoneNumber());
        zoneConstraint.setIsAllowed(createDto.getIsAllowed());
        zoneConstraint.setDescription(createDto.getDescription());

        if (createDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + createDto.getInternshipTypeId()));
            zoneConstraint.setInternshipType(internshipType);
        }

        return zoneConstraint;
    }

    @Override
    public ZoneConstraint toEntityUpdate(ZoneConstraintUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        ZoneConstraint zoneConstraint = new ZoneConstraint();
        zoneConstraint.setZoneNumber(updateDto.getZoneNumber());
        zoneConstraint.setIsAllowed(updateDto.getIsAllowed());
        zoneConstraint.setDescription(updateDto.getDescription());

        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + updateDto.getInternshipTypeId()));
            zoneConstraint.setInternshipType(internshipType);
        }

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
                zoneConstraint.getInternshipType() != null ? zoneConstraint.getInternshipType().getId() : null,
                zoneConstraint.getInternshipType() != null ? zoneConstraint.getInternshipType().getInternshipCode() : null,
                zoneConstraint.getInternshipType() != null ? zoneConstraint.getInternshipType().getFullName() : null,
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

        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + updateDto.getInternshipTypeId()));
            zoneConstraint.setInternshipType(internshipType);
        }

        if (updateDto.getIsAllowed() != null) {
            zoneConstraint.setIsAllowed(updateDto.getIsAllowed());
        }

        if (updateDto.getDescription() != null) {
            zoneConstraint.setDescription(updateDto.getDescription());
        }
    }
}