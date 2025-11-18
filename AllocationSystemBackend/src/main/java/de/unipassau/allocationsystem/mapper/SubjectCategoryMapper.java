package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.SubjectCategoryDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectCategoryMapper implements BaseMapper<SubjectCategory, SubjectCategoryDto> {

    @Override
    public SubjectCategory toEntity(SubjectCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        SubjectCategory entity = new SubjectCategory();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setCategoryTitle(dto.getCategoryTitle());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    @Override
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

    @Override
    public List<SubjectCategoryDto> toDtoList(List<SubjectCategory> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

