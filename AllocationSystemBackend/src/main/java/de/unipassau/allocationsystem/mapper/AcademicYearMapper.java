package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.AcademicYearDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AcademicYearMapper {

    public AcademicYear toEntity(AcademicYearDto dto) {
        if (dto == null) {
            return null;
        }
        return new AcademicYear(
                dto.getId(),
                dto.getYearName(),
                dto.getTotalCreditHours(),
                dto.getElementarySchoolHours(),
                dto.getMiddleSchoolHours(),
                dto.getBudgetAnnouncementDate(),
                dto.getAllocationDeadline(),
                dto.getIsLocked(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

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

    public List<AcademicYearDto> toDtoList(List<AcademicYear> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
