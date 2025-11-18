package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryCreateDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryResponseDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryUpdateDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectCategoryMapper implements BaseMapper<SubjectCategory, SubjectCategoryCreateDto, SubjectCategoryUpdateDto, SubjectCategoryResponseDto> {

    @Override
    public SubjectCategory toEntityCreate(SubjectCategoryCreateDto dto) {
        if (dto == null) return null;
        SubjectCategory entity = new SubjectCategory();
        entity.setCategoryTitle(dto.getCategoryTitle());
        return entity;
    }

    @Override
    public SubjectCategory toEntityUpdate(SubjectCategoryUpdateDto dto) {
        if (dto == null) return null;
        SubjectCategory entity = new SubjectCategory();
        entity.setCategoryTitle(dto.getCategoryTitle());
        return entity;
    }

    @Override
    public SubjectCategoryResponseDto toResponseDto(SubjectCategory entity) {
        if (entity == null) return null;
        return new SubjectCategoryResponseDto(
                entity.getId(),
                entity.getCategoryTitle(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<SubjectCategoryResponseDto> toResponseDtoList(List<SubjectCategory> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(SubjectCategoryUpdateDto dto, SubjectCategory entity) {
        if (dto == null || entity == null) return;
        if (dto.getCategoryTitle() != null) entity.setCategoryTitle(dto.getCategoryTitle());
    }
}
