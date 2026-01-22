package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpsertDto;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
        return toNewEntity((ZoneConstraintUpsertDto) createDto, ZoneConstraint::new, this::populateEntity);
    }

    @Override
    public ZoneConstraint toEntityUpdate(ZoneConstraintUpdateDto updateDto) {
        return toNewEntity((ZoneConstraintUpsertDto) updateDto, ZoneConstraint::new, this::populateEntity);
    }

    private void populateEntity(ZoneConstraint zoneConstraint, ZoneConstraintUpsertDto dto) {
        zoneConstraint.setZoneNumber(dto.getZoneNumber());
        zoneConstraint.setIsAllowed(dto.getIsAllowed());
        zoneConstraint.setDescription(dto.getDescription());

        if (dto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(dto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + dto.getInternshipTypeId()));
            zoneConstraint.setInternshipType(internshipType);
        }
    }

    @Override
    public ZoneConstraintResponseDto toResponseDto(ZoneConstraint zoneConstraint) {
        if (zoneConstraint == null) {
            return null;
        }
        InternshipType type = zoneConstraint.getInternshipType();
        return new ZoneConstraintResponseDto(
                zoneConstraint.getId(),
                zoneConstraint.getZoneNumber(),
                Optional.ofNullable(type).map(InternshipType::getId).orElse(null),
                Optional.ofNullable(type).map(InternshipType::getInternshipCode).orElse(null),
                Optional.ofNullable(type).map(InternshipType::getFullName).orElse(null),
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