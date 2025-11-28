package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandResponseDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InternshipDemandMapper implements BaseMapper<InternshipDemand, InternshipDemandCreateDto, InternshipDemandUpdateDto, InternshipDemandResponseDto> {

    @Override
    public InternshipDemand toEntityCreate(InternshipDemandCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        InternshipDemand d = new InternshipDemand();
        d.setRequiredTeachers(createDto.getRequiredTeachers());
        d.setStudentCount(createDto.getStudentCount());
        d.setIsForecasted(createDto.getIsForecasted() != null ? createDto.getIsForecasted() : Boolean.FALSE);
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
            r.setYearId(entity.getAcademicYear().getId());
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
    }
}
