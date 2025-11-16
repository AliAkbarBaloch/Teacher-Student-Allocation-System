package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import org.springframework.stereotype.Component;

/**
 * Mapper for School entity and DTOs.
 */
@Component
public class SchoolMapper {

    /**
     * Convert School entity to response DTO.
     */
    public SchoolResponseDto toResponseDto(School school) {
        if (school == null) {
            return null;
        }

        SchoolResponseDto dto = new SchoolResponseDto();
        dto.setId(school.getId());
        dto.setSchoolName(school.getSchoolName());
        dto.setSchoolType(school.getSchoolType());
        dto.setZoneNumber(school.getZoneNumber());
        dto.setAddress(school.getAddress());
        dto.setLatitude(school.getLatitude());
        dto.setLongitude(school.getLongitude());
        dto.setDistanceFromCenter(school.getDistanceFromCenter());
        dto.setTransportAccessibility(school.getTransportAccessibility());
        dto.setContactEmail(school.getContactEmail());
        dto.setContactPhone(school.getContactPhone());
        dto.setIsActive(school.getIsActive());
        dto.setCreatedAt(school.getCreatedAt());
        dto.setUpdatedAt(school.getUpdatedAt());
        return dto;
    }

    /**
     * Convert create DTO to School entity.
     */
    public School toEntity(SchoolCreateDto dto) {
        if (dto == null) {
            return null;
        }

        School school = new School();
        school.setSchoolName(dto.getSchoolName());
        school.setSchoolType(dto.getSchoolType());
        school.setZoneNumber(dto.getZoneNumber());
        school.setAddress(dto.getAddress());
        school.setLatitude(dto.getLatitude());
        school.setLongitude(dto.getLongitude());
        school.setDistanceFromCenter(dto.getDistanceFromCenter());
        school.setTransportAccessibility(dto.getTransportAccessibility());
        school.setContactEmail(dto.getContactEmail());
        school.setContactPhone(dto.getContactPhone());
        school.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return school;
    }

    /**
     * Update School entity from update DTO.
     * Only updates non-null fields.
     */
    public void updateEntityFromDto(SchoolUpdateDto dto, School school) {
        if (dto == null || school == null) {
            return;
        }

        if (dto.getSchoolName() != null) {
            school.setSchoolName(dto.getSchoolName());
        }
        if (dto.getSchoolType() != null) {
            school.setSchoolType(dto.getSchoolType());
        }
        if (dto.getZoneNumber() != null) {
            school.setZoneNumber(dto.getZoneNumber());
        }
        if (dto.getAddress() != null) {
            school.setAddress(dto.getAddress());
        }
        if (dto.getLatitude() != null) {
            school.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            school.setLongitude(dto.getLongitude());
        }
        if (dto.getDistanceFromCenter() != null) {
            school.setDistanceFromCenter(dto.getDistanceFromCenter());
        }
        if (dto.getTransportAccessibility() != null) {
            school.setTransportAccessibility(dto.getTransportAccessibility());
        }
        if (dto.getContactEmail() != null) {
            school.setContactEmail(dto.getContactEmail());
        }
        if (dto.getContactPhone() != null) {
            school.setContactPhone(dto.getContactPhone());
        }
    }
}
