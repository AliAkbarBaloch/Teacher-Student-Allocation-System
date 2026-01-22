package de.unipassau.allocationsystem.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.unipassau.allocationsystem.dto.subject.SubjectSimpleDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpsertDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;

/**
 * Mapper for converting between Teacher entity and DTOs.
 */
@Component
public class TeacherMapper implements BaseMapper<Teacher, TeacherCreateDto, TeacherUpdateDto, TeacherResponseDto> {

    @Override
    public Teacher toEntityCreate(TeacherCreateDto createDto) {
        return toNewEntity((TeacherUpsertDto) createDto, Teacher::new, this::populateEntity);
    }

    @Override
    public Teacher toEntityUpdate(TeacherUpdateDto updateDto) {
        return toNewEntity((TeacherUpsertDto) updateDto, Teacher::new, this::populateEntity);
    }

    private void populateEntity(Teacher teacher, TeacherUpsertDto dto) {
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        teacher.setPhone(dto.getPhone());
        teacher.setIsPartTime(dto.getIsPartTime());
        if (dto.getIsPartTime()) {
            teacher.setWorkingHoursPerWeek(dto.getWorkingHoursPerWeek());
        } else {
            teacher.setWorkingHoursPerWeek(0);
        }
        teacher.setEmploymentStatus(dto.getEmploymentStatus());
        teacher.setUsageCycle(dto.getUsageCycle());
    }

    @Override
    public TeacherResponseDto toResponseDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        School school = teacher.getSchool();
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .schoolId(Optional.ofNullable(school).map(School::getId).orElse(null))
                .schoolName(Optional.ofNullable(school).map(School::getSchoolName).orElse(null))
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .isPartTime(teacher.getIsPartTime())
                .workingHoursPerWeek(teacher.getWorkingHoursPerWeek())
                .employmentStatus(teacher.getEmploymentStatus())
                .usageCycle(teacher.getUsageCycle())
                .creditHourBalance(teacher.getCreditHourBalance())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .subjects(
                    Optional.ofNullable(teacher.getSubjects())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(subject -> new SubjectSimpleDto(
                            subject.getId(),
                            subject.getSubjectTitle()
                        ))
                        .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public List<TeacherResponseDto> toResponseDtoList(List<Teacher> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeacherUpdateDto updateDto, Teacher teacher) {
        if (updateDto == null || teacher == null) {
            return;
        }
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
        if (updateDto.getWorkingHoursPerWeek() != null) {
            teacher.setWorkingHoursPerWeek(updateDto.getWorkingHoursPerWeek());
        }
        if (updateDto.getEmploymentStatus() != null) {
            teacher.setEmploymentStatus(updateDto.getEmploymentStatus());
        }
        if (updateDto.getUsageCycle() != null) {
            teacher.setUsageCycle(updateDto.getUsageCycle());
        }
        // School should be set by service using schoolId if present
    }
}
