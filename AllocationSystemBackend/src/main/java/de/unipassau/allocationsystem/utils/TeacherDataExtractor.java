package de.unipassau.allocationsystem.utils;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.entity.Teacher;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;

/**
 * Helper class to extract and build teacher data from Excel row.
 */
class TeacherDataExtractor {
    private final Row row;
    private final ColumnIndices indices;
    private final String schoolName;
    private final Long schoolId;
    private final String phone;
    private final Teacher.EmploymentStatus employmentStatus;
    private final Boolean isPartTime;
    private final Teacher.UsageCycle usageCycle;

    TeacherDataExtractor(Row row, ColumnIndices indices) {
        this.row = row;
        this.indices = indices;
        this.schoolName = Optional.ofNullable(ExcelParser.getCellValue(row, indices.getSchoolNameIndex()))
                .map(String::trim).orElse(null);
        this.schoolId = ExcelParser.parseSchoolId(ExcelParser.getCellValue(row, indices.getSchoolIdIndex()));
        this.phone = ExcelParser.getCellValue(row, indices.getPhoneIndex());
        this.employmentStatus = ExcelParser.parseEmploymentStatus(
            Optional.ofNullable(ExcelParser.getCellValue(row, indices.getEmploymentStatusIndex())).orElse("FULL_TIME")
        );
        this.isPartTime = ExcelParser.parseBoolean(
            Optional.ofNullable(ExcelParser.getCellValue(row, indices.getIsPartTimeIndex())).orElse("false")
        );
        this.usageCycle = ExcelParser.parseUsageCycle(ExcelParser.getCellValue(row, indices.getUsageCycleIndex()));
    }

    TeacherCreateDto buildTeacherDto() {
        String firstName = getCellValueAsString(row.getCell(indices.getFirstNameIndex()));
        String lastName = getCellValueAsString(row.getCell(indices.getLastNameIndex()));
        String email = getCellValueAsString(row.getCell(indices.getEmailIndex()));

        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(schoolId);
        dto.setFirstName(firstName.trim());
        dto.setLastName(lastName.trim());
        dto.setEmail(email.trim());
        dto.setPhone(Optional.ofNullable(phone).filter(p -> !p.trim().isEmpty()).map(String::trim).orElse(null));
        dto.setIsPartTime(isPartTime);
        dto.setEmploymentStatus(employmentStatus);
        dto.setUsageCycle(usageCycle);
        return dto;
    }

    String getSchoolName() {
        return schoolName;
    }

    Long getSchoolId() {
        return schoolId;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }
}
