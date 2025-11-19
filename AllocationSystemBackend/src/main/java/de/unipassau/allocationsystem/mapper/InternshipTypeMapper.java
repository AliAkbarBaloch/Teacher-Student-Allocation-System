package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeCreateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeResponseDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InternshipTypeMapper implements BaseMapper<InternshipType, InternshipTypeCreateDto, InternshipTypeUpdateDto, InternshipTypeResponseDto> {

    @Override
    public InternshipType toEntityCreate(InternshipTypeCreateDto dto) {
        if (dto == null) {
            return null;
        }
        InternshipType entity = new InternshipType();
        entity.setInternshipCode(dto.getInternshipCode());
        entity.setFullName(dto.getFullName());
        entity.setTiming(dto.getTiming());
        entity.setPeriodType(dto.getPeriodType());
        entity.setSemester(dto.getSemester());
        entity.setIsSubjectSpecific(dto.getIsSubjectSpecific());
        entity.setPriorityOrder(dto.getPriorityOrder());
        return entity;
    }

    @Override
    public InternshipType toEntityUpdate(InternshipTypeUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        InternshipType entity = new InternshipType();
        entity.setInternshipCode(dto.getInternshipCode());
        entity.setFullName(dto.getFullName());
        entity.setTiming(dto.getTiming());
        entity.setPeriodType(dto.getPeriodType());
        entity.setSemester(dto.getSemester());
        entity.setIsSubjectSpecific(dto.getIsSubjectSpecific());
        entity.setPriorityOrder(dto.getPriorityOrder());
        return entity;
    }

    @Override
    public InternshipTypeResponseDto toResponseDto(InternshipType entity) {
        if (entity == null) {
            return null;
        }
        return new InternshipTypeResponseDto(
                entity.getId(),
                entity.getInternshipCode(),
                entity.getFullName(),
                entity.getTiming(),
                entity.getPeriodType(),
                entity.getSemester(),
                entity.getIsSubjectSpecific(),
                entity.getPriorityOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<InternshipTypeResponseDto> toResponseDtoList(List<InternshipType> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(InternshipTypeUpdateDto dto, InternshipType entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getInternshipCode() != null) {
            entity.setInternshipCode(dto.getInternshipCode());
        }
        if (dto.getFullName() != null) {
            entity.setFullName(dto.getFullName());
        }
        if (dto.getTiming() != null) {
            entity.setTiming(dto.getTiming());
        }
        if (dto.getPeriodType() != null) {
            entity.setPeriodType(dto.getPeriodType());
        }
        if (dto.getSemester() != null) {
            entity.setSemester(dto.getSemester());
        }
        if (dto.getIsSubjectSpecific() != null) {
            entity.setIsSubjectSpecific(dto.getIsSubjectSpecific());
        }
        if (dto.getPriorityOrder() != null) {
            entity.setPriorityOrder(dto.getPriorityOrder());
        }
    }
}
