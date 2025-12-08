import type {
  ParsedTeacherRow,
  RowValidationError,
  ValidationResult,
} from "../types/teacher.types";
import type { School } from "@/features/schools/types/school.types";
import { EMPLOYMENT_STATUS_OPTIONS } from "@/lib/constants/teachers";

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
  _rowIndex: number,
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

  // Validate employment status - use the actual valid statuses from constants
  if (!EMPLOYMENT_STATUS_OPTIONS.includes(row.employmentStatus)) {
    errors.push({
      rowNumber: row.rowNumber,
      field: "employmentStatus",
      message: `Invalid employment status: ${row.employmentStatus}. Valid values are: ${EMPLOYMENT_STATUS_OPTIONS.join(", ")}`,
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
 * Validate all rows and return validation result with chunked processing for better performance
 */
export async function validateAllRows(
  rows: ParsedTeacherRow[],
  schools: School[],
  existingEmails: Set<string> = new Set(),
  onProgress?: (progress: number) => void
): Promise<ValidationResult> {
  const schoolLookup = createSchoolLookupMap(schools);
  const emailSet = new Set<string>();
  const errors: RowValidationError[] = [];
  const validRows: ParsedTeacherRow[] = [];
  const invalidRows: ParsedTeacherRow[] = [];

  const CHUNK_SIZE = 50; // Validate 50 rows at a time
  const totalRows = rows.length;

  for (let i = 0; i < rows.length; i += CHUNK_SIZE) {
    const chunk = rows.slice(i, Math.min(i + CHUNK_SIZE, rows.length));
    
    chunk.forEach((row, chunkIndex) => {
      const index = i + chunkIndex;
      const rowErrors = validateRow(row, index, schoolLookup, emailSet, existingEmails);

      if (rowErrors.length > 0) {
        row.errors = rowErrors.map((e) => e.message);
        invalidRows.push(row);
        errors.push(...rowErrors);
      } else {
        validRows.push(row);
      }
    });

    // Update progress
    const progress = Math.floor(((i + chunk.length) / totalRows) * 100);
    onProgress?.(progress);

    // Yield to browser to prevent UI blocking
    if (i + CHUNK_SIZE < rows.length) {
      await new Promise(resolve => setTimeout(resolve, 0));
    }
  }

  return {
    validRows,
    invalidRows,
    errors,
    totalRows: rows.length,
  };
}

