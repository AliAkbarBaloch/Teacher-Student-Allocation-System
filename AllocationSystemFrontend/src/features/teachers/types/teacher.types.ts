  export type EmploymentStatus =
    | "ACTIVE"
    | "INACTIVE_THIS_YEAR"
    | "ON_LEAVE"
    | "ARCHIVED";

export type UsageCycle =
  | "GRADES_1_2"
  | "GRADES_3_4"
  | "GRADES_5_TO_9"
  | "FLEXIBLE";

export interface Teacher {
  id: number;
  schoolId: number;
  schoolName: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string | null;
  isPartTime: boolean;
  workingHoursPerWeek?: number | null;
  employmentStatus: EmploymentStatus;
  usageCycle?: UsageCycle | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface PaginatedTeacherResponse {
  items: Teacher[];
  totalItems: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

export interface TeacherFilters {
  search?: string;
  schoolId?: number;
  employmentStatus?: EmploymentStatus;
  isActive?: boolean;
}

export interface TeacherListParams extends TeacherFilters {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

export interface CreateTeacherRequest {
  schoolId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  isPartTime: boolean;
  workingHoursPerWeek?: number | null;
  employmentStatus: EmploymentStatus;
  usageCycle?: UsageCycle;
}

export interface UpdateTeacherRequest {
  schoolId?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  isPartTime?: boolean;
  workingHoursPerWeek?: number | null;
  employmentStatus?: EmploymentStatus;
  usageCycle?: UsageCycle;
}

export interface TeacherStatusUpdateRequest {
  status: EmploymentStatus;
}

export type TeacherFormErrors = Partial<Record<keyof CreateTeacherRequest, string>> & {
  general?: string;
};

export interface ApiErrorDetails {
  [key: string]: string | undefined;
}

export type ApiErrorResponse = Error & {
  status?: number;
  details?: unknown;
};

export function isApiError(error: unknown): error is ApiErrorResponse {
  return typeof error === "object" && error !== null && "message" in error;
}

// Bulk Import Types
export interface ParsedTeacherRow {
  rowNumber: number;
  schoolName?: string;
  schoolId?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  isPartTime: boolean;
  workingHoursPerWeek?: number | null;
  employmentStatus: EmploymentStatus;
  usageCycle?: UsageCycle;
  errors?: string[];
}

export interface RowValidationError {
  rowNumber: number;
  field: string;
  message: string;
  severity: "error" | "warning";
}

export interface ValidationResult {
  validRows: ParsedTeacherRow[];
  invalidRows: ParsedTeacherRow[];
  errors: RowValidationError[];
  totalRows: number;
}

export interface BulkImportResponse {
  totalRows: number;
  successfulRows: number;
  failedRows: number;
  results: ImportResultRow[];
}

export interface ImportResultRow {
  rowNumber: number;
  success: boolean;
  error?: string;
  teacher?: Teacher;
}

export type ImportStep = "upload" | "parsing" | "preview" | "validating" | "importing" | "results";

