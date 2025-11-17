package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.InternshipTypeDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InternshipTypeMapper {

    public InternshipType toEntity(InternshipTypeDto dto) {
        if (dto == null) {
            return null;
        }
        return new InternshipType(
                dto.getId(),
                dto.getInternshipCode(),
                dto.getFullName(),
                dto.getTiming(),
                dto.getPeriodType(),
                dto.getSemester(),
                dto.getIsSubjectSpecific(),
                dto.getPriorityOrder(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

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

    public List<InternshipTypeDto> toDtoList(List<InternshipType> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
