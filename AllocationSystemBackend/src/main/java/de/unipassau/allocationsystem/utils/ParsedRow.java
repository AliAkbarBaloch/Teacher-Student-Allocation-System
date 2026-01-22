package de.unipassau.allocationsystem.utils;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.entity.Teacher;
import lombok.Data;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;

/**
 * Represents a parsed row with its DTO and metadata.
 */
@Data
public class ParsedRow {
    private int rowNumber;
    private TeacherCreateDto dto;
    private String schoolName;
    private Long schoolId;
}

/**
 * Helper class to store column indices.
 */
@Getter
@Data
class ColumnIndices {
    private int schoolNameIndex = -1;
    private int schoolIdIndex = -1;
    private int firstNameIndex = -1;
    private int lastNameIndex = -1;
    private int emailIndex = -1;
    private int phoneIndex = -1;
    private int employmentStatusIndex = -1;
    private int isPartTimeIndex = -1;
    private int usageCycleIndex = -1;
}

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
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }
}

/**
 * Helper class for matching and setting column indices.
 */
class ColumnMatcher {
    static void matchAndSetColumns(String cellValue, int index, ColumnIndices indices) {
        if (indices.getSchoolNameIndex() == -1 && ExcelParser.matchesColumn(cellValue, 0, 3)) {
            indices.setSchoolNameIndex(index);
        } else if (indices.getSchoolIdIndex() == -1 && ExcelParser.matchesColumn(cellValue, 4, 6)) {
            indices.setSchoolIdIndex(index);
        } else if (indices.getFirstNameIndex() == -1 && ExcelParser.matchesColumn(cellValue, 7, 10)) {
            indices.setFirstNameIndex(index);
        } else if (indices.getLastNameIndex() == -1 && ExcelParser.matchesColumn(cellValue, 11, 14)) {
            indices.setLastNameIndex(index);
        } else if (indices.getEmailIndex() == -1 && ExcelParser.matchesColumn(cellValue, 15, 17)) {
            indices.setEmailIndex(index);
        } else if (indices.getPhoneIndex() == -1 && ExcelParser.matchesColumn(cellValue, 18, 21)) {
            indices.setPhoneIndex(index);
        } else if (indices.getEmploymentStatusIndex() == -1 && ExcelParser.matchesColumn(cellValue, 22, 25)) {
            indices.setEmploymentStatusIndex(index);
        } else if (indices.getIsPartTimeIndex() == -1 && ExcelParser.matchesColumn(cellValue, 26, 30)) {
            indices.setIsPartTimeIndex(index);
        } else if (indices.getUsageCycleIndex() == -1 && ExcelParser.matchesColumn(cellValue, 31, 34)) {
            indices.setUsageCycleIndex(index);
        }
    }
}

