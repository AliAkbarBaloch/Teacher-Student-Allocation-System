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
        AcademicYear entity = new AcademicYear();
        entity.setYearName(dto.getYearName());
        entity.setTotalCreditHours(dto.getTotalCreditHours());
        entity.setElementarySchoolHours(dto.getElementarySchoolHours());
        entity.setMiddleSchoolHours(dto.getMiddleSchoolHours());
        entity.setBudgetAnnouncementDate(dto.getBudgetAnnouncementDate());
        entity.setAllocationDeadline(dto.getAllocationDeadline());
        entity.setIsLocked(dto.getIsLocked());
        return entity;
    }

    @Override
    public AcademicYear toEntityUpdate(AcademicYearUpdateDto dto) {
        if (dto == null) {
             return null;
        }
        AcademicYear entity = new AcademicYear();
        entity.setYearName(dto.getYearName());
        entity.setTotalCreditHours(dto.getTotalCreditHours());
        entity.setElementarySchoolHours(dto.getElementarySchoolHours());
        entity.setMiddleSchoolHours(dto.getMiddleSchoolHours());
        entity.setBudgetAnnouncementDate(dto.getBudgetAnnouncementDate());
        entity.setAllocationDeadline(dto.getAllocationDeadline());
        entity.setIsLocked(dto.getIsLocked());
        return entity;
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
        if (dto.getYearName() != null) {
            entity.setYearName(dto.getYearName());
        }
        if (dto.getTotalCreditHours() != null) {
            entity.setTotalCreditHours(dto.getTotalCreditHours());
        }
        if (dto.getElementarySchoolHours() != null) {
            entity.setElementarySchoolHours(dto.getElementarySchoolHours());
        }
        if (dto.getMiddleSchoolHours() != null) {
            entity.setMiddleSchoolHours(dto.getMiddleSchoolHours());
        }
        if (dto.getBudgetAnnouncementDate() != null) {
            entity.setBudgetAnnouncementDate(dto.getBudgetAnnouncementDate());
        }
        if (dto.getAllocationDeadline() != null) {
            entity.setAllocationDeadline(dto.getAllocationDeadline());
        }
        if (dto.getIsLocked() != null) {
            entity.setIsLocked(dto.getIsLocked());
        }
    }
}
