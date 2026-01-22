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
    public static List<ParsedRow> parseExcelFile(MultipartFile file) throws IOException, IllegalArgumentException {
        try (InputStream inputStream = file.getInputStream(); 
             Workbook workbook = createWorkbook(inputStream, file.getOriginalFilename())) {
            
            Sheet sheet = validateAndGetSheet(workbook);
            ColumnIndices indices = parseAndValidateHeaders(sheet);
            return parseDataRows(sheet, indices);
            
        } catch (IOException e) {
            log.error("Error reading Excel file", e);
            throw e;
        }
    }
    
    private static Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName != null && fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        }
        return new XSSFWorkbook(inputStream);
    }
    
    private static Sheet validateAndGetSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet.getPhysicalNumberOfRows() < 2) {
            throw new IllegalArgumentException("Excel file must contain at least a header row and one data row");
        }
        return sheet;
    }
    
    private static ColumnIndices parseAndValidateHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        ColumnIndices indices = findColumnIndices(headerRow);
        
        if (indices.getFirstNameIndex() == -1 || indices.getLastNameIndex() == -1 || indices.getEmailIndex() == -1) {
            throw new IllegalArgumentException(
                "Missing required columns. Required: First Name, Last Name, Email. "
                + "Optional: School Name/ID, Employment Status, Is Part Time, Phone, Usage Cycle"
            );
        }
        return indices;
    }
    
    private static List<ParsedRow> parseDataRows(Sheet sheet, ColumnIndices indices) {
        List<ParsedRow> rows = new ArrayList<>();
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
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
            ColumnMatcher.matchAndSetColumns(cellValue, i, indices);
        }
        return indices;
    }

    static boolean matchesColumn(String value, int startIndex, int endIndex) {
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

        if (!areRequiredFieldsValid(firstName, lastName, email)) {
            return null;
        }

        TeacherDataExtractor extractor = new TeacherDataExtractor(row, indices);
        TeacherCreateDto dto = extractor.buildTeacherDto();

        ParsedRow parsedRow = new ParsedRow();
        parsedRow.setRowNumber(rowNumber);
        parsedRow.setDto(dto);
        parsedRow.setSchoolName(extractor.getSchoolName());
        parsedRow.setSchoolId(extractor.getSchoolId());

        return parsedRow;
    }
    
    private static boolean areRequiredFieldsValid(String firstName, String lastName, String email) {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    static String getCellValue(Row row, int index) {
        return index != -1 ? getCellValueAsString(row.getCell(index)) : null;
    }
    
    static Long parseSchoolId(String schoolIdStr) {
        if (schoolIdStr == null || schoolIdStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(schoolIdStr.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid school ID format: {}", schoolIdStr);
            return null;
        }
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

    static Boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String lower = value.trim().toLowerCase();
        return lower.equals("true") || lower.equals("yes") || lower.equals("1") || lower.equals("y");
    }

    private static <E extends Enum<E>> E safeEnumValueOf(String normalized, Class<E> enumClass, E defaultValue, String warningMsg) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(normalized)) {
                return constant;
            }
        }
        log.warn(warningMsg, normalized);
        return defaultValue;
    }

    static Teacher.EmploymentStatus parseEmploymentStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Teacher.EmploymentStatus.ACTIVE;
        }
        String normalized = value.trim().toUpperCase().replaceAll("[_\\s-]+", "_");
        return safeEnumValueOf(normalized, Teacher.EmploymentStatus.class, Teacher.EmploymentStatus.ACTIVE,
                "Invalid employment status: {}, using default ACTIVE");
    }

    static Teacher.UsageCycle parseUsageCycle(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replaceAll("[_\\s-]+", "_");
        return safeEnumValueOf(normalized, Teacher.UsageCycle.class, null,
                "Invalid usage cycle: {}, using null");
    }
}