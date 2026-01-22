package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeCreateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeResponseDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpdateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpsertDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
/**
 * Mapper for converting between InternshipType entities and DTOs.
 * Handles internship type creation, updates, and response transformations.
 */
public class InternshipTypeMapper implements BaseMapper<InternshipType, InternshipTypeCreateDto, InternshipTypeUpdateDto, InternshipTypeResponseDto> {

    @Override
    public InternshipType toEntityCreate(InternshipTypeCreateDto dto) {
        return toNewEntity(dto, InternshipType::new, this::populateEntity);
    }

    @Override
    public InternshipType toEntityUpdate(InternshipTypeUpdateDto dto) {
        return toNewEntity(dto, InternshipType::new, this::populateEntity);
    }

    /**
     * Populates entity from DTO using common interface.
     * 
     * @param entity Target entity
     * @param dto Source DTO (create or update)
     */
    private void populateEntity(InternshipType entity, InternshipTypeUpsertDto dto) {
        entity.setInternshipCode(dto.getInternshipCode());
        entity.setFullName(dto.getFullName());
        entity.setTiming(dto.getTiming());
        entity.setPeriodType(dto.getPeriodType());
        entity.setSemester(dto.getSemester());
        entity.setIsSubjectSpecific(dto.getIsSubjectSpecific());
        entity.setPriorityOrder(dto.getPriorityOrder());
    }

    @Override
    public InternshipTypeResponseDto toResponseDto(InternshipType entity) {
        return entity == null ? null : buildResponseDto(entity);
    }

    /**
     * Builds response DTO from entity.
     * 
     * @param entity Source entity
     * @return Response DTO
     */
    private InternshipTypeResponseDto buildResponseDto(InternshipType entity) {
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
        setIfNotNull(dto.getInternshipCode(), entity::setInternshipCode);
        setIfNotNull(dto.getFullName(), entity::setFullName);
        setIfNotNull(dto.getTiming(), entity::setTiming);
        setIfNotNull(dto.getPeriodType(), entity::setPeriodType);
        setIfNotNull(dto.getSemester(), entity::setSemester);
        setIfNotNull(dto.getIsSubjectSpecific(), entity::setIsSubjectSpecific);
        setIfNotNull(dto.getPriorityOrder(), entity::setPriorityOrder);
    }

    /**
     * Sets a value on entity if not null.
     * 
     * @param value Value to set
     * @param setter Setter method reference
     * @param <T> Type of value
     */
    private static <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
