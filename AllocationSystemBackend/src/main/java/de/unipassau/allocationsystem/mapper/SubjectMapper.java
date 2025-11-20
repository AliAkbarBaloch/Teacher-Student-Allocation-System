package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.subject.SubjectCreateDto;
import de.unipassau.allocationsystem.dto.subject.SubjectResponseDto;
import de.unipassau.allocationsystem.dto.subject.SubjectUpdateDto;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubjectMapper implements BaseMapper<Subject, SubjectCreateDto, SubjectUpdateDto, SubjectResponseDto> {

    private final SubjectCategoryRepository subjectCategoryRepository;

    @Override
    public Subject toEntityCreate(SubjectCreateDto dto) {
        if (dto == null) {
            return null;
        }
        Subject entity = new Subject();
        entity.setSubjectCode(dto.getSubjectCode());
        entity.setSubjectTitle(dto.getSubjectTitle());
        entity.setSchoolType(dto.getSchoolType());
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new RuntimeException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
        
        return entity;
    }

    @Override
    public Subject toEntityUpdate(SubjectUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        Subject entity = new Subject();
        entity.setSubjectCode(dto.getSubjectCode());
        entity.setSubjectTitle(dto.getSubjectTitle());
        entity.setSchoolType(dto.getSchoolType());
        entity.setIsActive(dto.getIsActive());
        
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new RuntimeException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
        
        return entity;
    }

    @Override
    public SubjectResponseDto toResponseDto(Subject entity) {
        if (entity == null) {
            return null;
        }
        return new SubjectResponseDto(
                entity.getId(),
                entity.getSubjectCode(),
                entity.getSubjectTitle(),
                entity.getSubjectCategory() != null ? entity.getSubjectCategory().getId() : null,
                entity.getSubjectCategory() != null ? entity.getSubjectCategory().getCategoryTitle() : null,
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
        if (dto.getSubjectCode() != null) {
            entity.setSubjectCode(dto.getSubjectCode());
        }
        if (dto.getSubjectTitle() != null) {
            entity.setSubjectTitle(dto.getSubjectTitle());
        }
        if (dto.getSubjectCategoryId() != null) {
            SubjectCategory category = subjectCategoryRepository.findById(dto.getSubjectCategoryId())
                    .orElseThrow(() -> new RuntimeException("Subject category not found with id: " + dto.getSubjectCategoryId()));
            entity.setSubjectCategory(category);
        }
        if (dto.getSchoolType() != null) {
            entity.setSchoolType(dto.getSchoolType());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}

