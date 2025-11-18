package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.AcademicYearDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AcademicYearMapper implements BaseMapper<AcademicYear, AcademicYearDto> {

    @Override
    public AcademicYear toEntity(AcademicYearDto dto) {
        if (dto == null) {
            return null;
        }
        AcademicYear entity = new AcademicYear();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setYearName(dto.getYearName());
        entity.setTotalCreditHours(dto.getTotalCreditHours());
        entity.setElementarySchoolHours(dto.getElementarySchoolHours());
        entity.setMiddleSchoolHours(dto.getMiddleSchoolHours());
        entity.setBudgetAnnouncementDate(dto.getBudgetAnnouncementDate());
        entity.setAllocationDeadline(dto.getAllocationDeadline());
        entity.setIsLocked(dto.getIsLocked());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    @Override
    public AcademicYearDto toDto(AcademicYear entity) {
        if (entity == null) {
            return null;
        }
        return new AcademicYearDto(
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
    public List<AcademicYearDto> toDtoList(List<AcademicYear> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
