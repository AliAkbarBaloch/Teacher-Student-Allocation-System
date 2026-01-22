package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.academicyear.AcademicYearCreateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
/**
 * Mapper for converting between AcademicYear entities and DTOs.
 * Handles academic year creation, updates, and response transformations.
 */
public class AcademicYearMapper implements BaseMapper<AcademicYear, AcademicYearCreateDto, AcademicYearUpdateDto, AcademicYearResponseDto> {

    @Override
    public AcademicYear toEntityCreate(AcademicYearCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return mapCommonFields(dto, new AcademicYear());
    }

    @Override
    public AcademicYear toEntityUpdate(AcademicYearUpdateDto dto) {
        if (dto == null) {
             return null;
        }
        return mapCommonFields(dto, new AcademicYear());
    }

    /**
     * Maps common fields from DTO to entity.
     * 
     * @param dto Source DTO containing academic year data
     * @param entity Target entity to populate
     * @return Populated entity
     */
    private AcademicYear mapCommonFields(Object dto, AcademicYear entity) {
        if (dto instanceof AcademicYearCreateDto createDto) {
            setEntityFields(entity, createDto.getYearName(), createDto.getTotalCreditHours(),
                    createDto.getElementarySchoolHours(), createDto.getMiddleSchoolHours(),
                    createDto.getBudgetAnnouncementDate(), createDto.getAllocationDeadline(),
                    createDto.getIsLocked());
        } else if (dto instanceof AcademicYearUpdateDto updateDto) {
            setEntityFields(entity, updateDto.getYearName(), updateDto.getTotalCreditHours(),
                    updateDto.getElementarySchoolHours(), updateDto.getMiddleSchoolHours(),
                    updateDto.getBudgetAnnouncementDate(), updateDto.getAllocationDeadline(),
                    updateDto.getIsLocked());
        }
        return entity;
    }

    /**
     * Sets entity fields from provided values.
     * 
     * @param entity Target entity
     * @param yearName Year name
     * @param totalCreditHours Total credit hours
     * @param elementarySchoolHours Elementary school hours
     * @param middleSchoolHours Middle school hours
     * @param budgetAnnouncementDate Budget announcement date
     * @param allocationDeadline Allocation deadline
     * @param isLocked Lock status
     */
    private void setEntityFields(AcademicYear entity, String yearName, Integer totalCreditHours,
                                 Integer elementarySchoolHours, Integer middleSchoolHours,
                                 java.time.LocalDateTime budgetAnnouncementDate,
                                 java.time.LocalDateTime allocationDeadline, Boolean isLocked) {
        if (yearName != null) {
            entity.setYearName(yearName);
        }
        if (totalCreditHours != null) {
            entity.setTotalCreditHours(totalCreditHours);
        }
        if (elementarySchoolHours != null) {
            entity.setElementarySchoolHours(elementarySchoolHours);
        }
        if (middleSchoolHours != null) {
            entity.setMiddleSchoolHours(middleSchoolHours);
        }
        if (budgetAnnouncementDate != null) {
            entity.setBudgetAnnouncementDate(budgetAnnouncementDate);
        }
        if (allocationDeadline != null) {
            entity.setAllocationDeadline(allocationDeadline);
        }
        if (isLocked != null) {
            entity.setIsLocked(isLocked);
        }
    }

    @Override
    public AcademicYearResponseDto toResponseDto(AcademicYear entity) {
        if (entity == null) {
             return null;
        }
        return new AcademicYearResponseDto(
                entity.getId(),
                entity.getYearName(),
                entity.getTotalCreditHours(),
                entity.getElementarySchoolHours(),
                entity.getMiddleSchoolHours(),
                entity.getBudgetAnnouncementDate(),
                entity.getAllocationDeadline(),
                entity.getIsLocked(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<AcademicYearResponseDto> toResponseDtoList(List<AcademicYear> entities) {
        if (entities == null) {
             return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(AcademicYearUpdateDto dto, AcademicYear entity) {
        if (dto == null || entity == null) {
            return;
        }
        setEntityFields(entity, dto.getYearName(), dto.getTotalCreditHours(),
                dto.getElementarySchoolHours(), dto.getMiddleSchoolHours(),
                dto.getBudgetAnnouncementDate(), dto.getAllocationDeadline(),
                dto.getIsLocked());
    }
}
