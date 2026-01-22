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
            HoursData hours = new HoursData(createDto.getTotalCreditHours(),
                    createDto.getElementarySchoolHours(), createDto.getMiddleSchoolHours());
            DateData dates = new DateData(createDto.getBudgetAnnouncementDate(),
                    createDto.getAllocationDeadline());
            setEntityFields(entity, createDto.getYearName(), hours, dates, createDto.getIsLocked());
        } else if (dto instanceof AcademicYearUpdateDto updateDto) {
            HoursData hours = new HoursData(updateDto.getTotalCreditHours(),
                    updateDto.getElementarySchoolHours(), updateDto.getMiddleSchoolHours());
            DateData dates = new DateData(updateDto.getBudgetAnnouncementDate(),
                    updateDto.getAllocationDeadline());
            setEntityFields(entity, updateDto.getYearName(), hours, dates, updateDto.getIsLocked());
        }
        return entity;
    }

    /**
     * Sets entity fields from provided values.
     * 
     * @param entity Target entity
     * @param yearName Year name
     * @param hours Credit hours data
     * @param dates Date information
     * @param isLocked Lock status
     */
    private void setEntityFields(AcademicYear entity, String yearName, HoursData hours,
                                 DateData dates, Boolean isLocked) {
        if (yearName != null) {
            entity.setYearName(yearName);
        }
        if (hours != null) {
            if (hours.totalCreditHours() != null) {
                entity.setTotalCreditHours(hours.totalCreditHours());
            }
            if (hours.elementarySchoolHours() != null) {
                entity.setElementarySchoolHours(hours.elementarySchoolHours());
            }
            if (hours.middleSchoolHours() != null) {
                entity.setMiddleSchoolHours(hours.middleSchoolHours());
            }
        }
        if (dates != null) {
            if (dates.budgetAnnouncementDate() != null) {
                entity.setBudgetAnnouncementDate(dates.budgetAnnouncementDate());
            }
            if (dates.allocationDeadline() != null) {
                entity.setAllocationDeadline(dates.allocationDeadline());
            }
        }
        if (isLocked != null) {
            entity.setIsLocked(isLocked);
        }
    }

    /**
     * Holds credit hours data for academic year.
     * 
     * @param totalCreditHours Total credit hours
     * @param elementarySchoolHours Elementary school hours
     * @param middleSchoolHours Middle school hours
     */
    private record HoursData(Integer totalCreditHours, Integer elementarySchoolHours,
                             Integer middleSchoolHours) {}

    /**
     * Holds date information for academic year.
     * 
     * @param budgetAnnouncementDate Budget announcement date
     * @param allocationDeadline Allocation deadline
     */
    private record DateData(java.time.LocalDateTime budgetAnnouncementDate,
                            java.time.LocalDateTime allocationDeadline) {}

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
        HoursData hours = new HoursData(dto.getTotalCreditHours(),
                dto.getElementarySchoolHours(), dto.getMiddleSchoolHours());
        DateData dates = new DateData(dto.getBudgetAnnouncementDate(),
                dto.getAllocationDeadline());
        setEntityFields(entity, dto.getYearName(), hours, dates, dto.getIsLocked());
    }
}
