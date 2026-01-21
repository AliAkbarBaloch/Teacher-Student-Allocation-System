package de.unipassau.allocationsystem.utils;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.entity.Teacher;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing Excel files containing teacher data.
 */
@Slf4j
public class ExcelParser {

    private static final String[] COLUMN_MAPPINGS = {
        "School Name", "School", "SchoolName", "school_name",
        "School ID", "SchoolId", "school_id",
        "First Name", "FirstName", "First", "first_name",
        "Last Name", "LastName", "Last", "last_name",
        "Email", "E-mail", "email",
        "Phone", "Phone Number", "PhoneNumber", "phone",
        "Employment Status", "EmploymentStatus", "Status", "employment_status",
        "Is Part Time", "IsPartTime", "Part Time", "part_time", "PartTime",
        "Usage Cycle", "UsageCycle", "Cycle", "usage_cycle"
    };

    /**
     * Parse Excel file and extract teacher data.
     *
     * @param file MultipartFile containing the Excel file
     * @return List of TeacherCreateDto objects parsed from the file
     * @throws IOException if file cannot be read
     */
    public static List<ParsedRow> parseExcelFile(MultipartFile file) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                workbook = new XSSFWorkbook(inputStream);
            }

            try {
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet.getPhysicalNumberOfRows() < 2) {
                    throw new IllegalArgumentException("Excel file must contain at least a header row and one data row");
                }

                // Parse header row
                Row headerRow = sheet.getRow(0);
                ColumnIndices indices = findColumnIndices(headerRow);

                // Validate required columns
                if (indices.getFirstNameIndex() == -1 || indices.getLastNameIndex() == -1 || indices.getEmailIndex() == -1) {
                    throw new IllegalArgumentException(
                        "Missing required columns. Required: First Name, Last Name, Email. "
                        + "Optional: School Name/ID, Employment Status, Is Part Time, Phone, Usage Cycle"
                    );
                }

                // Parse data rows
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    // Skip completely empty rows
                    if (isRowEmpty(row)) {
                        continue;
                    }

                    ParsedRow parsedRow = parseRow(row, indices, i + 1);
                    if (parsedRow != null) {
                        rows.add(parsedRow);
                    }
                }

                if (rows.isEmpty()) {
                    throw new IllegalArgumentException("No valid data rows found in Excel file");
                }
            } finally {
                workbook.close();
            }
        } catch (Exception e) {
            log.error("Error parsing Excel file", e);
            if (e instanceof IOException) {
                throw e;
            }
            throw new IllegalArgumentException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return rows;
    }

    private static ColumnIndices findColumnIndices(Row headerRow) {
        ColumnIndices indices = new ColumnIndices();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                continue;
            }

            String cellValue = getCellValueAsString(cell).trim();

            // School Name
            if (indices.getSchoolNameIndex() == -1 && matchesColumn(cellValue, 0, 3)) {
                indices.setSchoolNameIndex(i);
            }
            // School ID
            if (indices.getSchoolIdIndex() == -1 && matchesColumn(cellValue, 4, 6)) {
                indices.setSchoolIdIndex(i);
            }
            // First Name
            if (indices.getFirstNameIndex() == -1 && matchesColumn(cellValue, 7, 10)) {
                indices.setFirstNameIndex(i);
            }
            // Last Name
            if (indices.getLastNameIndex() == -1 && matchesColumn(cellValue, 11, 14)) {
                indices.setLastNameIndex(i);
            }
            // Email
            if (indices.getEmailIndex() == -1 && matchesColumn(cellValue, 15, 17)) {
                indices.setEmailIndex(i);
            }
            // Phone
            if (indices.getPhoneIndex() == -1 && matchesColumn(cellValue, 18, 21)) {
                indices.setPhoneIndex(i);
            }
            // Employment Status
            if (indices.getEmploymentStatusIndex() == -1 && matchesColumn(cellValue, 22, 25)) {
                indices.setEmploymentStatusIndex(i);
            }
            // Is Part Time
            if (indices.getIsPartTimeIndex() == -1 && matchesColumn(cellValue, 26, 30)) {
                indices.setIsPartTimeIndex(i);
            }
            // Usage Cycle
            if (indices.getUsageCycleIndex() == -1 && matchesColumn(cellValue, 31, 34)) {
                indices.setUsageCycleIndex(i);
            }
        }

        return indices;
    }

    private static boolean matchesColumn(String value, int startIndex, int endIndex) {
        String normalized = value.toLowerCase().replaceAll("[\\s_-]+", "");
        for (int i = startIndex; i <= endIndex && i < COLUMN_MAPPINGS.length; i++) {
            String mapping = COLUMN_MAPPINGS[i].toLowerCase().replaceAll("[\\s_-]+", "");
            if (normalized.equals(mapping)) {
                return true;
            }
        }
        return false;
    }

    private static ParsedRow parseRow(Row row, ColumnIndices indices, int rowNumber) {
        String firstName = getCellValueAsString(row.getCell(indices.getFirstNameIndex()));
        String lastName = getCellValueAsString(row.getCell(indices.getLastNameIndex()));
        String email = getCellValueAsString(row.getCell(indices.getEmailIndex()));

        // Skip rows with missing required fields
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return null;
        }

        String schoolName = indices.getSchoolNameIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getSchoolNameIndex()))
            : null;
        String schoolIdStr = indices.getSchoolIdIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getSchoolIdIndex()))
            : null;
        Long schoolId = null;
        if (schoolIdStr != null && !schoolIdStr.trim().isEmpty()) {
            try {
                schoolId = Long.parseLong(schoolIdStr.trim());
            } catch (NumberFormatException e) {
                // Invalid school ID, will be handled in validation
            }
        }

        String phone = indices.getPhoneIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getPhoneIndex()))
            : null;

        String employmentStatusStr = indices.getEmploymentStatusIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getEmploymentStatusIndex()))
            : "FULL_TIME";
        Teacher.EmploymentStatus employmentStatus = parseEmploymentStatus(employmentStatusStr);

        String isPartTimeStr = indices.getIsPartTimeIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getIsPartTimeIndex()))
            : "false";
        Boolean isPartTime = parseBoolean(isPartTimeStr);

        String usageCycleStr = indices.getUsageCycleIndex() != -1
            ? getCellValueAsString(row.getCell(indices.getUsageCycleIndex()))
            : null;
        Teacher.UsageCycle usageCycle = parseUsageCycle(usageCycleStr);

        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(schoolId); // Will be resolved from school name if needed
        dto.setFirstName(firstName.trim());
        dto.setLastName(lastName.trim());
        dto.setEmail(email.trim());
        dto.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
        dto.setIsPartTime(isPartTime);
        dto.setEmploymentStatus(employmentStatus);
        dto.setUsageCycle(usageCycle);

        ParsedRow parsedRow = new ParsedRow();
        parsedRow.setRowNumber(rowNumber);
        parsedRow.setDto(dto);
        parsedRow.setSchoolName(schoolName != null ? schoolName.trim() : null);
        parsedRow.setSchoolId(schoolId);

        return parsedRow;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String lower = value.trim().toLowerCase();
        return lower.equals("true") || lower.equals("yes") || lower.equals("1") || lower.equals("y");
    }

    private static Teacher.EmploymentStatus parseEmploymentStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Teacher.EmploymentStatus.ACTIVE;
        }
        try {
            String normalized = value.trim().toUpperCase().replaceAll("[_\\s-]+", "_");
            return Teacher.EmploymentStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return Teacher.EmploymentStatus.ACTIVE; // Default
        }
    }

    private static Teacher.UsageCycle parseUsageCycle(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String normalized = value.trim().toUpperCase().replaceAll("[_\\s-]+", "_");
            return Teacher.UsageCycle.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Helper class to store column indices.
     */
    private static class ColumnIndices {
        private int schoolNameIndex = -1;
        private int schoolIdIndex = -1;
        private int firstNameIndex = -1;
        private int lastNameIndex = -1;
        private int emailIndex = -1;
        private int phoneIndex = -1;
        private int employmentStatusIndex = -1;
        private int isPartTimeIndex = -1;
        private int usageCycleIndex = -1;

        public int getSchoolNameIndex() {
            return schoolNameIndex;
        }

        public void setSchoolNameIndex(int schoolNameIndex) {
            this.schoolNameIndex = schoolNameIndex;
        }

        public int getSchoolIdIndex() {
            return schoolIdIndex;
        }

        public void setSchoolIdIndex(int schoolIdIndex) {
            this.schoolIdIndex = schoolIdIndex;
        }

        public int getFirstNameIndex() {
            return firstNameIndex;
        }

        public void setFirstNameIndex(int firstNameIndex) {
            this.firstNameIndex = firstNameIndex;
        }

        public int getLastNameIndex() {
            return lastNameIndex;
        }

        public void setLastNameIndex(int lastNameIndex) {
            this.lastNameIndex = lastNameIndex;
        }

        public int getEmailIndex() {
            return emailIndex;
        }

        public void setEmailIndex(int emailIndex) {
            this.emailIndex = emailIndex;
        }

        public int getPhoneIndex() {
            return phoneIndex;
        }

        public void setPhoneIndex(int phoneIndex) {
            this.phoneIndex = phoneIndex;
        }

        public int getEmploymentStatusIndex() {
            return employmentStatusIndex;
        }

        public void setEmploymentStatusIndex(int employmentStatusIndex) {
            this.employmentStatusIndex = employmentStatusIndex;
        }

        public int getIsPartTimeIndex() {
            return isPartTimeIndex;
        }

        public void setIsPartTimeIndex(int isPartTimeIndex) {
            this.isPartTimeIndex = isPartTimeIndex;
        }

        public int getUsageCycleIndex() {
            return usageCycleIndex;
        }

        public void setUsageCycleIndex(int usageCycleIndex) {
            this.usageCycleIndex = usageCycleIndex;
        }
    }

    /**
     * Represents a parsed row with its DTO and metadata.
     */
    public static class ParsedRow {
        private int rowNumber;
        private TeacherCreateDto dto;
        private String schoolName;
        private Long schoolId;

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public TeacherCreateDto getDto() {
            return dto;
        }

        public void setDto(TeacherCreateDto dto) {
            this.dto = dto;
        }

        public String getSchoolName() {
            return schoolName;
        }

        public void setSchoolName(String schoolName) {
            this.schoolName = schoolName;
        }

        public Long getSchoolId() {
            return schoolId;
        }

        public void setSchoolId(Long schoolId) {
            this.schoolId = schoolId;
        }
    }
}

