import type {
  ParsedTeacherRow,
  RowValidationError,
  ValidationResult,
  EmploymentStatus,
} from "../types/teacher.types";
import type { School } from "@/features/schools/types/school.types";

/**
 * Email validation regex
 */
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Phone validation regex (flexible format)
 */
const PHONE_REGEX = /^[\d\s+\-()]+$/;

/**
 * Validate email format
 */
function isValidEmail(email: string): boolean {
  return EMAIL_REGEX.test(email);
}

/**
 * Validate phone format (optional field)
 */
function isValidPhone(phone: string | undefined): boolean {
  if (!phone) return true; // Optional field
  return PHONE_REGEX.test(phone) && phone.trim().length >= 7;
}

/**
 * Create a school lookup map for efficient validation
 */
function createSchoolLookupMap(schools: School[]): Map<string | number, School> {
  const map = new Map<string | number, School>();
  schools.forEach((school) => {
    map.set(school.id, school);
    map.set(school.schoolName.toLowerCase(), school);
  });
  return map;
}

/**
 * Validate a single row
 */
export function validateRow(
  row: ParsedTeacherRow,
  rowIndex: number,
  schoolLookup: Map<string | number, School>,
  emailSet: Set<string>,
  existingEmails: Set<string> = new Set()
): RowValidationError[] {
  const errors: RowValidationError[] = [];

  // Validate required fields
  if (!row.firstName || row.firstName.trim() === "") {
    errors.push({
      rowNumber: row.rowNumber,
      field: "firstName",
      message: "First name is required",
      severity: "error",
    });
  }

  if (!row.lastName || row.lastName.trim() === "") {
    errors.push({
      rowNumber: row.rowNumber,
      field: "lastName",
      message: "Last name is required",
      severity: "error",
    });
  }

  if (!row.email || row.email.trim() === "") {
    errors.push({
      rowNumber: row.rowNumber,
      field: "email",
      message: "Email is required",
      severity: "error",
    });
  } else if (!isValidEmail(row.email)) {
    errors.push({
      rowNumber: row.rowNumber,
      field: "email",
      message: "Invalid email format",
      severity: "error",
    });
  } else {
    const normalizedEmail = row.email.toLowerCase().trim();
    
    // Check for duplicate emails within the file
    if (emailSet.has(normalizedEmail)) {
      errors.push({
        rowNumber: row.rowNumber,
        field: "email",
        message: "Duplicate email in file",
        severity: "error",
      });
    } else {
      emailSet.add(normalizedEmail);
      
      // Check if email already exists in database
      if (existingEmails.has(normalizedEmail)) {
        errors.push({
          rowNumber: row.rowNumber,
          field: "email",
          message: "Email already exists in database",
          severity: "error",
        });
      }
    }
  }

  // Validate school (either name or ID must be provided and valid)
  if (!row.schoolName && !row.schoolId) {
    errors.push({
      rowNumber: row.rowNumber,
      field: "school",
      message: "Either School Name or School ID is required",
      severity: "error",
    });
  } else {
    let schoolFound = false;
    if (row.schoolId) {
      schoolFound = schoolLookup.has(row.schoolId);
      if (!schoolFound) {
        errors.push({
          rowNumber: row.rowNumber,
          field: "schoolId",
          message: `School with ID ${row.schoolId} not found`,
          severity: "error",
        });
      }
    }
    if (row.schoolName && !schoolFound) {
      const school = schoolLookup.get(row.schoolName.toLowerCase());
      if (!school) {
        errors.push({
          rowNumber: row.rowNumber,
          field: "schoolName",
          message: `School "${row.schoolName}" not found`,
          severity: "error",
        });
      } else {
        // Set schoolId if we found it by name
        row.schoolId = school.id;
      }
    }
  }

  // Validate employment status
  const validEmploymentStatuses: EmploymentStatus[] = [
    "FULL_TIME",
    "PART_TIME",
    "ON_LEAVE",
    "CONTRACT",
    "PROBATION",
    "RETIRED",
  ];
  if (!validEmploymentStatuses.includes(row.employmentStatus)) {
    errors.push({
      rowNumber: row.rowNumber,
      field: "employmentStatus",
      message: `Invalid employment status: ${row.employmentStatus}`,
      severity: "error",
    });
  }

  // Validate phone (optional field)
  if (row.phone && !isValidPhone(row.phone)) {
    errors.push({
      rowNumber: row.rowNumber,
      field: "phone",
      message: "Invalid phone format",
      severity: "warning",
    });
  }

  return errors;
}

/**
 * Validate all rows and return validation result
 */
export function validateAllRows(
  rows: ParsedTeacherRow[],
  schools: School[],
  existingEmails: Set<string> = new Set()
): ValidationResult {
  const schoolLookup = createSchoolLookupMap(schools);
  const emailSet = new Set<string>();
  const errors: RowValidationError[] = [];
  const validRows: ParsedTeacherRow[] = [];
  const invalidRows: ParsedTeacherRow[] = [];

  rows.forEach((row, index) => {
    const rowErrors = validateRow(row, index, schoolLookup, emailSet, existingEmails);

    if (rowErrors.length > 0) {
      row.errors = rowErrors.map((e) => e.message);
      invalidRows.push(row);
      errors.push(...rowErrors);
    } else {
      validRows.push(row);
    }
  });

  return {
    validRows,
    invalidRows,
    errors,
    totalRows: rows.length,
  };
}

