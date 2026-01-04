package de.unipassau.allocationsystem.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.unipassau.allocationsystem.dto.subject.SubjectSimpleDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;

/**
 * Mapper for converting between Teacher entity and DTOs.
 */
@Component
public class TeacherMapper implements BaseMapper<Teacher, TeacherCreateDto, TeacherUpdateDto, TeacherResponseDto> {

    @Override
    public Teacher toEntityCreate(TeacherCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        Teacher teacher = new Teacher();
        // School should be set by service using schoolId
        teacher.setFirstName(createDto.getFirstName());
        teacher.setLastName(createDto.getLastName());
        teacher.setEmail(createDto.getEmail());
        teacher.setPhone(createDto.getPhone());
        teacher.setIsPartTime(createDto.getIsPartTime());
        if (createDto.getIsPartTime()) {
            teacher.setWorkingHoursPerWeek(createDto.getWorkingHoursPerWeek());
        } else {
            teacher.setWorkingHoursPerWeek(0);
        }
        teacher.setEmploymentStatus(createDto.getEmploymentStatus());
        teacher.setUsageCycle(createDto.getUsageCycle());
        teacher.setEmploymentStatus(createDto.getEmploymentStatus());
        return teacher;
    }

    @Override
    public Teacher toEntityUpdate(TeacherUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        Teacher teacher = new Teacher();
        teacher.setFirstName(updateDto.getFirstName());
        teacher.setLastName(updateDto.getLastName());
        teacher.setEmail(updateDto.getEmail());
        teacher.setPhone(updateDto.getPhone());
        teacher.setIsPartTime(updateDto.getIsPartTime());
        if (updateDto.getIsPartTime()) {
            teacher.setWorkingHoursPerWeek(updateDto.getWorkingHoursPerWeek());
        } else {
            teacher.setWorkingHoursPerWeek(0);
        }
        teacher.setEmploymentStatus(updateDto.getEmploymentStatus());
        teacher.setUsageCycle(updateDto.getUsageCycle());
        // School should be set by service using schoolId if present
        return teacher;
    }

    @Override
    public TeacherResponseDto toResponseDto(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        School school = teacher.getSchool();
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .schoolId(school != null ? school.getId() : null)
                .schoolName(school != null ? school.getSchoolName() : null)
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
                //subjects 
                .subjects(
                    teacher.getSubjects() == null
                    ? Collections.emptyList()
                    : teacher.getSubjects().stream()
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
