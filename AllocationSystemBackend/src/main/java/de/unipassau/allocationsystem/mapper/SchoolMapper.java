package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpsertDto;
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
        if (createDto == null) return null;
        School school = new School();
        populateEntity(school, (SchoolUpsertDto) createDto);
        school.setIsActive(createDto.getIsActive() != null ? createDto.getIsActive() : true);
        school.setCreatedAt(createDto.getCreatedAt());
        school.setUpdatedAt(createDto.getUpdatedAt());
        return school;
    }

    @Override
    public School toEntityUpdate(SchoolUpdateDto updateDto) {
        if (updateDto == null) return null;
        School school = new School();
        populateEntity(school, (SchoolUpsertDto) updateDto);
        return school;
    }

    private void populateEntity(School school, SchoolUpsertDto dto) {
        school.setSchoolName(dto.getSchoolName());
        // Convert SchoolType enum
        if (dto.getSchoolType() != null) {
            school.setSchoolType(dto.getSchoolType());
        }
        school.setZoneNumber(dto.getZoneNumber());
        school.setAddress(dto.getAddress());
        // Set BigDecimal values directly
        if (dto.getLatitude() != null) {
            school.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            school.setLongitude(dto.getLongitude());
        }
        if (dto.getDistanceFromCenter() != null) {
            school.setDistanceFromCenter(dto.getDistanceFromCenter());
        }
        school.setTransportAccessibility(dto.getTransportAccessibility());
        school.setContactEmail(dto.getContactEmail());
        school.setContactPhone(dto.getContactPhone());
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

        updateBasicFields(updateDto, school);
        updateLocationFields(updateDto, school);
        updateContactFields(updateDto, school);
    }

    private void updateBasicFields(SchoolUpdateDto updateDto, School school) {
        if (updateDto.getSchoolName() != null) {
            school.setSchoolName(updateDto.getSchoolName());
        }
        if (updateDto.getSchoolType() != null) {
            school.setSchoolType(updateDto.getSchoolType());
        }
        if (updateDto.getZoneNumber() != null) {
            school.setZoneNumber(updateDto.getZoneNumber());
        }
    }

    private void updateLocationFields(SchoolUpdateDto updateDto, School school) {
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
    }

    private void updateContactFields(SchoolUpdateDto updateDto, School school) {
        if (updateDto.getContactEmail() != null) {
            school.setContactEmail(updateDto.getContactEmail());
        }
        if (updateDto.getContactPhone() != null) {
            school.setContactPhone(updateDto.getContactPhone());
        }
    }
}
