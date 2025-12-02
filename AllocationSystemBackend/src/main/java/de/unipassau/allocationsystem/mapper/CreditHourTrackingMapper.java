package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingCreateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreditHourTrackingMapper implements BaseMapper<CreditHourTracking, CreditHourTrackingCreateDto, CreditHourTrackingUpdateDto, CreditHourTrackingResponseDto> {

    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public CreditHourTracking toEntityCreate(CreditHourTrackingCreateDto dto) {
        if (dto == null) {
            return null;
        }
        CreditHourTracking entity = new CreditHourTracking();
        entity.setAssignmentsCount(dto.getAssignmentsCount());
        entity.setCreditHoursAllocated(dto.getCreditHoursAllocated());
        entity.setCreditBalance(dto.getCreditBalance());
        entity.setNotes(dto.getNotes());

        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
            entity.setTeacher(teacher);
        }
        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + dto.getAcademicYearId()));
            entity.setAcademicYear(year);
        }

        return entity;
    }

    @Override
    public CreditHourTracking toEntityUpdate(CreditHourTrackingUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        CreditHourTracking entity = new CreditHourTracking();
        entity.setAssignmentsCount(dto.getAssignmentsCount());
        entity.setCreditHoursAllocated(dto.getCreditHoursAllocated());
        entity.setCreditBalance(dto.getCreditBalance());
        entity.setNotes(dto.getNotes());

        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
            entity.setTeacher(teacher);
        }
        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + dto.getAcademicYearId()));
            entity.setAcademicYear(year);
        }

        return entity;
    }

    @Override
    public CreditHourTrackingResponseDto toResponseDto(CreditHourTracking entity) {
        if (entity == null) {
            return null;
        }
        return new CreditHourTrackingResponseDto(
                entity.getId(),
                entity.getTeacher() != null ? entity.getTeacher().getId() : null,
                entity.getTeacher() != null ? (entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName()) : null,
                entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null,
                entity.getAcademicYear() != null ? entity.getAcademicYear().getYearName() : null,
                entity.getAssignmentsCount(),
                entity.getCreditHoursAllocated(),
                entity.getCreditBalance(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<CreditHourTrackingResponseDto> toResponseDtoList(List<CreditHourTracking> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(CreditHourTrackingUpdateDto dto, CreditHourTracking entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getAssignmentsCount() != null) {
            entity.setAssignmentsCount(dto.getAssignmentsCount());
        }
        if (dto.getCreditHoursAllocated() != null) {
            entity.setCreditHoursAllocated(dto.getCreditHoursAllocated());
        }
        if (dto.getCreditBalance() != null) {
            entity.setCreditBalance(dto.getCreditBalance());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
            entity.setTeacher(teacher);
        }
        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + dto.getAcademicYearId()));
            entity.setAcademicYear(year);
        }
    }
}