package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.SubjectCategoryDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectCategoryMapper {

    public SubjectCategory toEntity(SubjectCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return new SubjectCategory(
                dto.getId(),
                dto.getCategoryTitle(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

    public SubjectCategoryDto toDto(SubjectCategory entity) {
        if (entity == null) {
            return null;
        }
        return new SubjectCategoryDto(
                entity.getId(),
                entity.getCategoryTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<SubjectCategoryDto> toDtoList(List<SubjectCategory> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}

