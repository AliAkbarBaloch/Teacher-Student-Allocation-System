package de.unipassau.allocationsystem.utils;

import lombok.Data;
import lombok.Getter;

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
