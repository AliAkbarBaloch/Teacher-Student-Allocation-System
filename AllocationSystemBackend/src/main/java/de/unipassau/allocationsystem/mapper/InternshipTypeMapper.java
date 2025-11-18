package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.InternshipTypeDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InternshipTypeMapper implements BaseMapper<InternshipType, InternshipTypeDto> {

    @Override
    public InternshipType toEntity(InternshipTypeDto dto) {
        if (dto == null) {
            return null;
        }
        InternshipType entity = new InternshipType();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setInternshipCode(dto.getInternshipCode());
        entity.setFullName(dto.getFullName());
        entity.setTiming(dto.getTiming());
        entity.setPeriodType(dto.getPeriodType());
        entity.setSemester(dto.getSemester());
        entity.setIsSubjectSpecific(dto.getIsSubjectSpecific());
        entity.setPriorityOrder(dto.getPriorityOrder());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    @Override
    public InternshipTypeDto toDto(InternshipType entity) {
        if (entity == null) {
            return null;
        }
        return new InternshipTypeDto(
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
    public List<InternshipTypeDto> toDtoList(List<InternshipType> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
