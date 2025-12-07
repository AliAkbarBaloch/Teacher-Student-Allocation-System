import * as XLSX from "xlsx";
import type { ParsedTeacherRow, EmploymentStatus, UsageCycle } from "../types/teacher.types";
import { EMPLOYMENT_STATUS_OPTIONS, USAGE_CYCLE_OPTIONS } from "@/lib/constants/teachers";

/**
 * Column mapping for Excel file
 * Supports multiple possible column names for flexibility
 */
const COLUMN_MAPPINGS = {
  schoolName: ["School Name", "School", "SchoolName", "school_name"],
  schoolId: ["School ID", "SchoolId", "school_id"],
  firstName: ["First Name", "FirstName", "First", "first_name"],
  lastName: ["Last Name", "LastName", "Last", "last_name"],
  email: ["Email", "E-mail", "email"],
  phone: ["Phone", "Phone Number", "PhoneNumber", "phone"],
  employmentStatus: ["Employment Status", "EmploymentStatus", "Status", "employment_status"],
  isPartTime: ["Is Part Time", "IsPartTime", "Part Time", "part_time", "PartTime"],
  usageCycle: ["Usage Cycle", "UsageCycle", "Cycle", "usage_cycle"],
} as const;

/**
 * Normalize column name by trimming and removing extra spaces
 */
function normalizeColumnName(name: string): string {
  return name.trim().replace(/\s+/g, " ");
}

/**
 * Find column index by matching against possible names
 */
function findColumnIndex(headers: string[], possibleNames: readonly string[]): number {
  const normalizedHeaders = headers.map(normalizeColumnName);
  for (const name of possibleNames) {
    const normalizedName = normalizeColumnName(name);
    const index = normalizedHeaders.findIndex(
      (h) => h.toLowerCase() === normalizedName.toLowerCase()
    );
    if (index !== -1) return index;
  }
  return -1;
}

/**
 * Parse boolean value from various formats
 */
function parseBoolean(value: unknown): boolean {
  if (typeof value === "boolean") return value;
  if (typeof value === "number") return value !== 0;
  if (typeof value === "string") {
    const lower = value.toLowerCase().trim();
    return lower === "true" || lower === "yes" || lower === "1" || lower === "y";
  }
  return false;
}

/**
 * Parse employment status from string
 */
function parseEmploymentStatus(value: unknown): EmploymentStatus | null {
  if (typeof value !== "string") return null;
  const normalized = value.trim().toUpperCase().replace(/[_\s-]/g, "_");
  const validStatuses: EmploymentStatus[] = EMPLOYMENT_STATUS_OPTIONS;
  return validStatuses.includes(normalized as EmploymentStatus) ? (normalized as EmploymentStatus) : null;
}

/**
 * Parse usage cycle from string
 */
function parseUsageCycle(value: unknown): UsageCycle | null {
  if (typeof value === "undefined" || value === null || value === "") return null;
  if (typeof value !== "string") return null;
  const normalized = value.trim().toUpperCase().replace(/[_\s-]/g, "_");
  const validCycles: UsageCycle[] = USAGE_CYCLE_OPTIONS;
  return validCycles.includes(normalized as UsageCycle) ? (normalized as UsageCycle) : null;
}

/**
 * Get cell value as string, handling empty cells
 */
function getCellValue(row: unknown[], index: number): string {
  if (index < 0 || index >= row.length) return "";
  const value = row[index];
  if (value === null || value === undefined) return "";
  return String(value).trim();
}

/**
 * Parse Excel file and extract teacher data
 */
export async function parseExcelFile(file: File): Promise<ParsedTeacherRow[]> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: "array" });

        // Get first sheet
        const firstSheetName = workbook.SheetNames[0];
        if (!firstSheetName) {
          reject(new Error("Excel file contains no sheets"));
          return;
        }

        const worksheet = workbook.Sheets[firstSheetName];
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1, defval: "" });

        if (jsonData.length < 2) {
          reject(new Error("Excel file must contain at least a header row and one data row"));
          return;
        }

        // Parse headers
        const headers = (jsonData[0] as unknown[]).map((h) => String(h));
        const columnIndices = {
          schoolName: findColumnIndex(headers, COLUMN_MAPPINGS.schoolName),
          schoolId: findColumnIndex(headers, COLUMN_MAPPINGS.schoolId),
          firstName: findColumnIndex(headers, COLUMN_MAPPINGS.firstName),
          lastName: findColumnIndex(headers, COLUMN_MAPPINGS.lastName),
          email: findColumnIndex(headers, COLUMN_MAPPINGS.email),
          phone: findColumnIndex(headers, COLUMN_MAPPINGS.phone),
          employmentStatus: findColumnIndex(headers, COLUMN_MAPPINGS.employmentStatus),
          isPartTime: findColumnIndex(headers, COLUMN_MAPPINGS.isPartTime),
          usageCycle: findColumnIndex(headers, COLUMN_MAPPINGS.usageCycle),
        };

        // Validate required columns
        if (columnIndices.firstName === -1 || columnIndices.lastName === -1 || columnIndices.email === -1) {
          reject(
            new Error(
              "Missing required columns. Required: First Name, Last Name, Email. Optional: School Name/ID, Employment Status, Is Part Time, Phone, Usage Cycle"
            )
          );
          return;
        }

        // Parse data rows
        const rows: ParsedTeacherRow[] = [];
        for (let i = 1; i < jsonData.length; i++) {
          const row = jsonData[i] as unknown[];

          // Skip completely empty rows
          if (row.every((cell) => !cell || String(cell).trim() === "")) continue;

          const firstName = getCellValue(row, columnIndices.firstName);
          const lastName = getCellValue(row, columnIndices.lastName);
          const email = getCellValue(row, columnIndices.email);

          // Skip rows with missing required fields
          if (!firstName || !lastName || !email) continue;

          const schoolName = columnIndices.schoolName !== -1 ? getCellValue(row, columnIndices.schoolName) : undefined;
          const schoolIdStr =
            columnIndices.schoolId !== -1 ? getCellValue(row, columnIndices.schoolId) : undefined;
          const schoolId = schoolIdStr ? Number.parseInt(schoolIdStr, 10) : undefined;
          const phone = columnIndices.phone !== -1 ? getCellValue(row, columnIndices.phone) : undefined;

          const employmentStatusValue =
            columnIndices.employmentStatus !== -1
              ? getCellValue(row, columnIndices.employmentStatus)
              : "";
          const employmentStatus = parseEmploymentStatus(employmentStatusValue) || "ACTIVE"; // Default

          const isPartTimeValue =
            columnIndices.isPartTime !== -1 ? getCellValue(row, columnIndices.isPartTime) : "false";
          const isPartTime = parseBoolean(isPartTimeValue);

          const usageCycleValue =
            columnIndices.usageCycle !== -1 ? getCellValue(row, columnIndices.usageCycle) : "";
          const usageCycle = parseUsageCycle(usageCycleValue) || undefined;

          rows.push({
            rowNumber: i + 1, // Excel row number (1-indexed, +1 for header)
            schoolName,
            schoolId: schoolId && !Number.isNaN(schoolId) ? schoolId : undefined,
            firstName,
            lastName,
            email,
            phone: phone || undefined,
            isPartTime,
            employmentStatus,
            usageCycle,
          });
        }

        if (rows.length === 0) {
          reject(new Error("No valid data rows found in Excel file"));
          return;
        }

        resolve(rows);
      } catch (error) {
        reject(error instanceof Error ? error : new Error("Failed to parse Excel file"));
      }
    };

    reader.onerror = () => {
      reject(new Error("Failed to read file"));
    };

    reader.readAsArrayBuffer(file);
  });
}

