package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeacherAvailabilityMapper implements BaseMapper<TeacherAvailability, TeacherAvailabilityCreateDto, TeacherAvailabilityUpdateDto, TeacherAvailabilityResponseDto> {

    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final InternshipTypeRepository internshipTypeRepository;

    public TeacherAvailabilityMapper(
            TeacherRepository teacherRepository,
            AcademicYearRepository academicYearRepository,
            InternshipTypeRepository internshipTypeRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.academicYearRepository = academicYearRepository;
        this.internshipTypeRepository = internshipTypeRepository;
    }

    @Override
    public TeacherAvailability toEntityCreate(TeacherAvailabilityCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        TeacherAvailability entity = new TeacherAvailability();
        entity.setStatus(createDto.getStatus());
        entity.setIsAvailable(createDto.getIsAvailable() != null ? createDto.getIsAvailable() : 
                (createDto.getStatus() != TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE));
        entity.setPreferenceRank(createDto.getPreferenceRank());
        entity.setNotes(createDto.getNotes());

        Teacher teacher = teacherRepository.findById(createDto.getTeacherId()).orElse(null);
        AcademicYear year = academicYearRepository.findById(createDto.getAcademicYearId()).orElse(null);
        InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId()).orElse(null);

        entity.setTeacher(teacher);
        entity.setAcademicYear(year);
        entity.setInternshipType(internshipType);

        return entity;
    }

    @Override
    public TeacherAvailability toEntityUpdate(TeacherAvailabilityUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        TeacherAvailability entity = new TeacherAvailability();
        entity.setStatus(updateDto.getStatus());
        entity.setPreferenceRank(updateDto.getPreferenceRank());
        entity.setNotes(updateDto.getNotes());

        if (updateDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(updateDto.getTeacherId()).orElse(null);
            entity.setTeacher(teacher);
        }
        if (updateDto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(updateDto.getAcademicYearId()).orElse(null);
            entity.setAcademicYear(year);
        }
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId()).orElse(null);
            entity.setInternshipType(internshipType);
        }

        return entity;
    }

    @Override
    public TeacherAvailabilityResponseDto toResponseDto(TeacherAvailability entity) {
        if (entity == null) {
            return null;
        }
        return TeacherAvailabilityResponseDto.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacherFirstName(entity.getTeacher() != null ? entity.getTeacher().getFirstName() : null)
                .teacherLastName(entity.getTeacher() != null ? entity.getTeacher().getLastName() : null)
                .teacherEmail(entity.getTeacher() != null ? entity.getTeacher().getEmail() : null)
                .academicYearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .academicYearName(entity.getAcademicYear() != null ? entity.getAcademicYear().getYearName() : null)
                .internshipTypeId(entity.getInternshipType() != null ? entity.getInternshipType().getId() : null)
                .internshipTypeName(entity.getInternshipType() != null ? entity.getInternshipType().getFullName() : null)
                .internshipTypeCode(entity.getInternshipType() != null ? entity.getInternshipType().getInternshipCode() : null)
                .status(entity.getStatus())
                .isAvailable(entity.getIsAvailable())
                .preferenceRank(entity.getPreferenceRank())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public List<TeacherAvailabilityResponseDto> toResponseDtoList(List<TeacherAvailability> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeacherAvailabilityUpdateDto updateDto, TeacherAvailability entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        if (updateDto.getStatus() != null) {
            entity.setStatus(updateDto.getStatus());
            if (updateDto.getStatus() == TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE) {
                entity.setPreferenceRank(null);
            }
        }
        if (updateDto.getPreferenceRank() != null || updateDto.getStatus() == TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE) {
            entity.setPreferenceRank(updateDto.getPreferenceRank());
        }
        if (updateDto.getNotes() != null) {
            entity.setNotes(updateDto.getNotes());
        }
        if (updateDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(updateDto.getTeacherId()).orElse(null);
            entity.setTeacher(teacher);
        }
        if (updateDto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(updateDto.getAcademicYearId()).orElse(null);
            entity.setAcademicYear(year);
        }
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId()).orElse(null);
            entity.setInternshipType(internshipType);
        }
    }
}
