package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import org.springframework.stereotype.Component;

/**
 * Mapper for TeacherAvailability entity and DTOs.
 */
@Component
public class TeacherAvailabilityMapper {

    /**
     * Convert TeacherAvailability entity to response DTO with denormalized fields.
     *
     * @param entity TeacherAvailability entity
     * @return TeacherAvailabilityResponseDto
     */
    public TeacherAvailabilityResponseDto toDto(TeacherAvailability entity) {
        if (entity == null) {
            return null;
        }

        return TeacherAvailabilityResponseDto.builder()
                .availabilityId(entity.getAvailabilityId())
                .teacherId(entity.getTeacher().getId())
                .teacherFirstName(entity.getTeacher().getFirstName())
                .teacherLastName(entity.getTeacher().getLastName())
                .teacherEmail(entity.getTeacher().getEmail())
                .yearId(entity.getAcademicYear().getId())
                .yearName(entity.getAcademicYear().getYearName())
                .internshipTypeId(entity.getInternshipType().getId())
                .internshipTypeName(entity.getInternshipType().getFullName())
                .internshipTypeCode(entity.getInternshipType().getInternshipCode())
                .isAvailable(entity.getIsAvailable())
                .preferenceRank(entity.getPreferenceRank())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Update entity from DTO. Only updates non-null fields.
     *
     * @param entity TeacherAvailability entity to update
     * @param dto TeacherAvailabilityUpdateDto with new values
     */
    public void updateEntityFromDto(TeacherAvailability entity, TeacherAvailabilityUpdateDto dto) {
        if (dto == null) {
            return;
        }

        // Note: yearId, teacherId, internshipTypeId are updated in service layer
        // Only availability status, preference rank, and notes can be updated here
        
        if (dto.getIsAvailable() != null) {
            entity.setIsAvailable(dto.getIsAvailable());
            // If setting to not available, also clear preference rank
            if (Boolean.FALSE.equals(dto.getIsAvailable())) {
                entity.setPreferenceRank(null);
            }
        }
        // Always update preference rank if provided (even if null, to allow clearing it)
        // Only skip if not provided at all - but for DTOs, we need to check if field was set
        // Since we can't distinguish between "not set" and "set to null" in Java,
        // we handle this in conjunction with isAvailable flag above
        if (dto.getPreferenceRank() != null || Boolean.FALSE.equals(dto.getIsAvailable())) {
            entity.setPreferenceRank(dto.getPreferenceRank());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}
