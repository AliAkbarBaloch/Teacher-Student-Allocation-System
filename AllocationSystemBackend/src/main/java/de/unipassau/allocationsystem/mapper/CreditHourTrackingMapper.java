package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingCreateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpsertDto;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/**
 * Mapper for converting between CreditHourTracking entities and DTOs.
 * Handles credit hour tracking with teacher and academic year resolution.
 */
public class CreditHourTrackingMapper implements BaseMapper<CreditHourTracking, CreditHourTrackingCreateDto, CreditHourTrackingUpdateDto, CreditHourTrackingResponseDto> {

    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public CreditHourTracking toEntityCreate(CreditHourTrackingCreateDto dto) {
        return toNewEntity(dto, CreditHourTracking::new, this::populateEntity);
    }

    @Override
    public CreditHourTracking toEntityUpdate(CreditHourTrackingUpdateDto dto) {
        return toNewEntity(dto, CreditHourTracking::new, this::populateEntity);
    }

    /**
     * Populates entity from DTO using common interface.
     * 
     * @param entity Target entity
     * @param dto Source DTO (create or update)
     */
    private void populateEntity(CreditHourTracking entity, CreditHourTrackingUpsertDto dto) {
        entity.setAssignmentsCount(dto.getAssignmentsCount());
        entity.setCreditHoursAllocated(dto.getCreditHoursAllocated());
        entity.setCreditBalance(dto.getCreditBalance());
        entity.setNotes(dto.getNotes());

        resolveAndSetTeacher(entity, dto.getTeacherId());
        resolveAndSetAcademicYear(entity, dto.getAcademicYearId());
    }

    /**
     * Resolves and sets teacher entity.
     * 
     * @param entity Target entity
     * @param teacherId Teacher ID
     */
    private void resolveAndSetTeacher(CreditHourTracking entity, Long teacherId) {
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));
            entity.setTeacher(teacher);
        }
    }

    /**
     * Resolves and sets academic year entity.
     * 
     * @param entity Target entity
     * @param academicYearId Academic year ID
     */
    private void resolveAndSetAcademicYear(CreditHourTracking entity, Long academicYearId) {
        if (academicYearId != null) {
            AcademicYear year = academicYearRepository.findById(academicYearId)
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));
            entity.setAcademicYear(year);
        }
    }

    @Override
    public CreditHourTrackingResponseDto toResponseDto(CreditHourTracking entity) {
        return entity == null ? null : buildResponseDto(entity);
    }

    /**
     * Builds response DTO from entity.
     * 
     * @param entity Source entity
     * @return Response DTO
     */
    private CreditHourTrackingResponseDto buildResponseDto(CreditHourTracking entity) {
        Teacher teacher = entity.getTeacher();
        AcademicYear year = entity.getAcademicYear();
        return new CreditHourTrackingResponseDto(
                entity.getId(),
                Optional.ofNullable(teacher).map(Teacher::getId).orElse(null),
                Optional.ofNullable(teacher).map(t -> t.getFirstName() + " " + t.getLastName()).orElse(null),
                Optional.ofNullable(year).map(AcademicYear::getId).orElse(null),
                Optional.ofNullable(year).map(AcademicYear::getYearName).orElse(null),
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
        setIfNotNull(dto.getAssignmentsCount(), entity::setAssignmentsCount);
        setIfNotNull(dto.getCreditHoursAllocated(), entity::setCreditHoursAllocated);
        setIfNotNull(dto.getCreditBalance(), entity::setCreditBalance);
        setIfNotNull(dto.getNotes(), entity::setNotes);
        
        resolveAndSetTeacher(entity, dto.getTeacherId());
        resolveAndSetAcademicYear(entity, dto.getAcademicYearId());
    }

    /**
     * Sets a value on entity if not null.
     * 
     * @param value Value to set
     * @param setter Setter method reference
     * @param <T> Type of value
     */
    private static <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}