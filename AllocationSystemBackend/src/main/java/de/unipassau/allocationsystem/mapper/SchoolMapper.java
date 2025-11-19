package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for School entity and DTOs.
 */
@Component
public class SchoolMapper implements BaseMapper<School, SchoolCreateDto, SchoolUpdateDto, SchoolResponseDto> {

    @Override
    public School toEntityCreate(SchoolCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        School school = new School();
        school.setSchoolName(createDto.getSchoolName());
        school.setSchoolType(createDto.getSchoolType());
        school.setZoneNumber(createDto.getZoneNumber());
        school.setAddress(createDto.getAddress());
        school.setLatitude(createDto.getLatitude());
        school.setLongitude(createDto.getLongitude());
        school.setDistanceFromCenter(createDto.getDistanceFromCenter());
        school.setTransportAccessibility(createDto.getTransportAccessibility());
        school.setContactEmail(createDto.getContactEmail());
        school.setContactPhone(createDto.getContactPhone());
        school.setIsActive(createDto.getIsActive() != null ? createDto.getIsActive() : true);
        school.setCreatedAt(createDto.getCreatedAt());
        school.setUpdatedAt(createDto.getUpdatedAt());
        return school;
    }

    @Override
    public School toEntityUpdate(SchoolUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        School school = new School();
        school.setSchoolName(updateDto.getSchoolName());
        school.setSchoolType(updateDto.getSchoolType());
        school.setZoneNumber(updateDto.getZoneNumber());
        school.setAddress(updateDto.getAddress());
        school.setLatitude(updateDto.getLatitude());
        school.setLongitude(updateDto.getLongitude());
        school.setDistanceFromCenter(updateDto.getDistanceFromCenter());
        school.setTransportAccessibility(updateDto.getTransportAccessibility());
        school.setContactEmail(updateDto.getContactEmail());
        school.setContactPhone(updateDto.getContactPhone());
        return school;
    }

    @Override
    public SchoolResponseDto toResponseDto(School school) {
        if (school == null) {
            return null;
        }
        return new SchoolResponseDto(
                school.getId(),
                school.getSchoolName(),
                school.getSchoolType(),
                school.getZoneNumber(),
                school.getAddress(),
                school.getLatitude(),
                school.getLongitude(),
                school.getDistanceFromCenter(),
                school.getTransportAccessibility(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getIsActive(),
                school.getCreatedAt(),
                school.getUpdatedAt()
        );
    }

    @Override
    public List<SchoolResponseDto> toResponseDtoList(List<School> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(SchoolUpdateDto updateDto, School school) {
        if (updateDto == null || school == null) {
            return;
        }

        if (updateDto.getSchoolName() != null) {
            school.setSchoolName(updateDto.getSchoolName());
        }
        if (updateDto.getSchoolType() != null) {
            school.setSchoolType(updateDto.getSchoolType());
        }
        if (updateDto.getZoneNumber() != null) {
            school.setZoneNumber(updateDto.getZoneNumber());
        }
        if (updateDto.getAddress() != null) {
            school.setAddress(updateDto.getAddress());
        }
        if (updateDto.getLatitude() != null) {
            school.setLatitude(updateDto.getLatitude());
        }
        if (updateDto.getLongitude() != null) {
            school.setLongitude(updateDto.getLongitude());
        }
        if (updateDto.getDistanceFromCenter() != null) {
            school.setDistanceFromCenter(updateDto.getDistanceFromCenter());
        }
        if (updateDto.getTransportAccessibility() != null) {
            school.setTransportAccessibility(updateDto.getTransportAccessibility());
        }
        if (updateDto.getContactEmail() != null) {
            school.setContactEmail(updateDto.getContactEmail());
        }
        if (updateDto.getContactPhone() != null) {
            school.setContactPhone(updateDto.getContactPhone());
        }
    }
}
