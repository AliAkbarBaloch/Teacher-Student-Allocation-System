package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.subject.SubjectCreateDto;
import de.unipassau.allocationsystem.dto.subject.SubjectResponseDto;
import de.unipassau.allocationsystem.dto.subject.SubjectUpdateDto;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.util.MapperUtil;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/**
 * Mapper for converting between Subject entities and DTOs.
 * Handles subject mapping with subject category resolution.
 */
public class SubjectMapper implements BaseMapper<Subject, SubjectCreateDto, SubjectUpdateDto, SubjectResponseDto> {

    private final SubjectCategoryRepository subjectCategoryRepository;

    @Override
    public Subject toEntityCreate(SubjectCreateDto dto) {
        return toNewEntity(dto, Subject::new, this::populateEntityCreate);
    }

    @Override
    public Subject toEntityUpdate(SubjectUpdateDto dto) {
        return toNewEntity(dto, Subject::new, this::populateEntityUpdate);
    }

    private void populateEntityCreate(Subject entity, SubjectCreateDto dto) {
        entity.setSubjectCode(dto.getSubjectCode());
        entity.setSubjectTitle(dto.getSubjectTitle());
        entity.setSchoolType(dto.getSchoolType());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
    }

    private void populateEntityUpdate(Subject entity, SubjectUpdateDto dto) {
        entity.setSubjectCode(dto.getSubjectCode());
        entity.setSubjectTitle(dto.getSubjectTitle());
        entity.setSchoolType(dto.getSchoolType());
        entity.setIsActive(dto.getIsActive());
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
    }

    @Override
    public SubjectResponseDto toResponseDto(Subject entity) {
        if (entity == null) {
            return null;
        }
        SubjectCategory category = entity.getSubjectCategory();
        return new SubjectResponseDto(
                entity.getId(),
                entity.getSubjectCode(),
                entity.getSubjectTitle(),
                Optional.ofNullable(category).map(SubjectCategory::getId).orElse(null),
                Optional.ofNullable(category).map(SubjectCategory::getCategoryTitle).orElse(null),
                entity.getSchoolType(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<SubjectResponseDto> toResponseDtoList(List<Subject> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(SubjectUpdateDto dto, Subject entity) {
        if (dto == null || entity == null) {
            return;
        }
        MapperUtil.setIfNotNull(dto.getSubjectCode(), entity::setSubjectCode);
        MapperUtil.setIfNotNull(dto.getSubjectTitle(), entity::setSubjectTitle);
        
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
        
        MapperUtil.setIfNotNull(dto.getSchoolType(), entity::setSchoolType);
        MapperUtil.setIfNotNull(dto.getIsActive(), entity::setIsActive);
    }
}

