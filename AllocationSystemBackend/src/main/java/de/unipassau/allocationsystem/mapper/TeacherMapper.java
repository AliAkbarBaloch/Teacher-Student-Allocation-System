package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.Teacher;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Teacher entity and DTOs.
 */
@Component
public class TeacherMapper {

    /**
     * Convert Teacher entity to response DTO.
     *
     * @param teacher Teacher entity
     * @return TeacherResponseDto
     */
    public TeacherResponseDto toDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .schoolId(teacher.getSchool().getId())
                .schoolName(teacher.getSchool().getSchoolName())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .isPartTime(teacher.getIsPartTime())
                .employmentStatus(teacher.getEmploymentStatus())
                .usageCycle(teacher.getUsageCycle())
                .isActive(teacher.getIsActive())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    /**
     * Update existing Teacher entity with data from update DTO.
     * Only updates non-null fields.
     *
     * @param teacher   Existing teacher entity
     * @param updateDto Update DTO
     */
    public void updateEntityFromDto(Teacher teacher, TeacherUpdateDto updateDto) {
        if (updateDto.getFirstName() != null) {
            teacher.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            teacher.setLastName(updateDto.getLastName());
        }
        if (updateDto.getEmail() != null) {
            teacher.setEmail(updateDto.getEmail());
        }
        if (updateDto.getPhone() != null) {
            teacher.setPhone(updateDto.getPhone());
        }
        if (updateDto.getIsPartTime() != null) {
            teacher.setIsPartTime(updateDto.getIsPartTime());
        }
        if (updateDto.getEmploymentStatus() != null) {
            teacher.setEmploymentStatus(updateDto.getEmploymentStatus());
        }
        if (updateDto.getUsageCycle() != null) {
            teacher.setUsageCycle(updateDto.getUsageCycle());
        }
    }
}
