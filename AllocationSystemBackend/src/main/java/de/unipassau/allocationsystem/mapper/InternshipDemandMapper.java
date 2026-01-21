package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandResponseDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/**
 * Mapper for converting between InternshipDemand entities and DTOs.
 * Handles demand tracking with academic year, internship type, and subject resolution.
 */
public class InternshipDemandMapper implements BaseMapper<InternshipDemand, InternshipDemandCreateDto, InternshipDemandUpdateDto, InternshipDemandResponseDto> {

    private final AcademicYearRepository academicYearRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public InternshipDemand toEntityCreate(InternshipDemandCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        InternshipDemand d = new InternshipDemand();
        d.setRequiredTeachers(createDto.getRequiredTeachers());
        d.setStudentCount(createDto.getStudentCount());
        d.setIsForecasted(createDto.getIsForecasted() != null ? createDto.getIsForecasted() : Boolean.FALSE);

        if (createDto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(createDto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + createDto.getAcademicYearId()));
            d.setAcademicYear(year);
        }
        if (createDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + createDto.getInternshipTypeId()));
            d.setInternshipType(internshipType);
        }
        if (createDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(createDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + createDto.getSubjectId()));
            d.setSubject(subject);
        }
        d.setSchoolType(SchoolType.valueOf(createDto.getSchoolType()));

        return d;
    }

    @Override
    public InternshipDemand toEntityUpdate(InternshipDemandUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        InternshipDemand d = new InternshipDemand();
        d.setRequiredTeachers(updateDto.getRequiredTeachers());
        d.setStudentCount(updateDto.getStudentCount());
        d.setIsForecasted(updateDto.getIsForecasted());

        if (updateDto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(updateDto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + updateDto.getAcademicYearId()));
            d.setAcademicYear(year);
        }
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + updateDto.getInternshipTypeId()));
            d.setInternshipType(internshipType);
        }
        if (updateDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updateDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + updateDto.getSubjectId()));
            d.setSubject(subject);
        }
        d.setSchoolType(SchoolType.valueOf(updateDto.getSchoolType()));

        return d;
    }

    @Override
    public InternshipDemandResponseDto toResponseDto(InternshipDemand entity) {
        if (entity == null) {
            return null;
        }
        InternshipDemandResponseDto r = new InternshipDemandResponseDto();
        r.setId(entity.getId());
        if (entity.getAcademicYear() != null) {
            r.setAcademicYearId(entity.getAcademicYear().getId());
        }
        if (entity.getInternshipType() != null) {
            r.setInternshipTypeId(entity.getInternshipType().getId());
            r.setInternshipTypeCode(entity.getInternshipType().getInternshipCode());
        }
        if (entity.getSubject() != null) {
            r.setSubjectId(entity.getSubject().getId());
            r.setSubjectCode(entity.getSubject().getSubjectCode());
        }
        r.setSchoolType(entity.getSchoolType() != null ? entity.getSchoolType().name() : null);
        r.setRequiredTeachers(entity.getRequiredTeachers());
        r.setStudentCount(entity.getStudentCount());
        r.setIsForecasted(entity.getIsForecasted());
        r.setCreatedAt(entity.getCreatedAt());
        r.setUpdatedAt(entity.getUpdatedAt());
        return r;
    }

    @Override
    public List<InternshipDemandResponseDto> toResponseDtoList(List<InternshipDemand> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(InternshipDemandUpdateDto updateDto, InternshipDemand entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        if (updateDto.getRequiredTeachers() != null) {
            entity.setRequiredTeachers(updateDto.getRequiredTeachers());
        }
        if (updateDto.getStudentCount() != null) {
            entity.setStudentCount(updateDto.getStudentCount());
        }
        if (updateDto.getIsForecasted() != null) {
            entity.setIsForecasted(updateDto.getIsForecasted());
        }
        if (updateDto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(updateDto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + updateDto.getAcademicYearId()));
            entity.setAcademicYear(year);
        }
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + updateDto.getInternshipTypeId()));
            entity.setInternshipType(internshipType);
        }
        if (updateDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updateDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + updateDto.getSubjectId()));
            entity.setSubject(subject);
        }
        if (updateDto.getSchoolType() != null) {
            entity.setSchoolType(SchoolType.valueOf(updateDto.getSchoolType()));
        }
    }
}