package de.unipassau.allocationsystem.utils;

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
