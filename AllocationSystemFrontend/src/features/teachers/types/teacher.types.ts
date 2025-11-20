export type EmploymentStatus = "FULL_TIME" | "PART_TIME" | "ON_LEAVE" | "CONTRACT" | "PROBATION" | "RETIRED";

export type UsageCycle = "SEMESTER_1" | "SEMESTER_2" | "FULL_YEAR" | "QUARTERLY";

export interface Teacher {
  id: number;
  schoolId: number;
  schoolName: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string | null;
  isPartTime: boolean;
  employmentStatus: EmploymentStatus;
  usageCycle?: UsageCycle | null;
  isActive: boolean;
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
  employmentStatus: EmploymentStatus;
  usageCycle?: UsageCycle;
  // Note: isActive is not in the backend create DTO - teachers are created as active by default
}

export interface UpdateTeacherRequest {
  schoolId?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  isPartTime?: boolean;
  employmentStatus?: EmploymentStatus;
  usageCycle?: UsageCycle;
}

export interface TeacherStatusUpdateRequest {
  isActive: boolean;
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

