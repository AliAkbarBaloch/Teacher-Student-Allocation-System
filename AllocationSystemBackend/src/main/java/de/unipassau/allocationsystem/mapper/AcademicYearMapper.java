package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.academicyear.AcademicYearCreateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpdateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpsertDto;
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
        return toNewEntity(dto, AcademicYear::new, this::populateEntity);
    }

    @Override
    public AcademicYear toEntityUpdate(AcademicYearUpdateDto dto) {
        return toNewEntity(dto, AcademicYear::new, this::populateEntity);
    }

    /**
     * Populates entity from DTO using common interface.
     * 
     * @param entity Target entity
     * @param dto Source DTO (create or update)
     */
    private void populateEntity(AcademicYear entity, AcademicYearUpsertDto dto) {
        HoursData hours = new HoursData(dto.getTotalCreditHours(),
                dto.getElementarySchoolHours(), dto.getMiddleSchoolHours());
        DateData dates = new DateData(dto.getBudgetAnnouncementDate(),
                dto.getAllocationDeadline());
        setEntityFields(entity, dto.getYearName(), hours, dates, dto.getIsLocked());
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
        setIfNotNull(yearName, entity::setYearName);

        if (hours != null) {
            setIfNotNull(hours.totalCreditHours(), entity::setTotalCreditHours);
            setIfNotNull(hours.elementarySchoolHours(), entity::setElementarySchoolHours);
            setIfNotNull(hours.middleSchoolHours(), entity::setMiddleSchoolHours);
        }

        if (dates != null) {
            setIfNotNull(dates.budgetAnnouncementDate(), entity::setBudgetAnnouncementDate);
            setIfNotNull(dates.allocationDeadline(), entity::setAllocationDeadline);
        }

        setIfNotNull(isLocked, entity::setIsLocked);
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
        return AcademicYearResponseDto.fromEntity(entity);
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
        populateEntity(entity, dto);
    }
}
