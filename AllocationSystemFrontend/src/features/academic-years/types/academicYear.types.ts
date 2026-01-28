/**
 * AcademicYear management types.
 */

/**
 * Represents an academic year entity.
 */
export interface AcademicYear {
  id: number;
  yearName: string;
  totalCreditHours: number;
  elementarySchoolHours: number;
  middleSchoolHours: number;
  budgetAnnouncementDate: string; // ISO string
  allocationDeadline?: string | null; // ISO string or null
  isLocked?: boolean | null;
  createdAt: string;
  updatedAt?: string | null;
}

/**
 * Request payload for creating a new academic year.
 */
export interface CreateAcademicYearRequest {
  yearName: string;
  totalCreditHours: number;
  elementarySchoolHours: number;
  middleSchoolHours: number;
  budgetAnnouncementDate: string;
  allocationDeadline?: string | null;
  isLocked?: boolean | null;
}

/**
 * Request payload for updating an existing academic year.
 */
export interface UpdateAcademicYearRequest {
  yearName?: string;
  totalCreditHours?: number;
  elementarySchoolHours?: number;
  middleSchoolHours?: number;
  budgetAnnouncementDate?: string;
  allocationDeadline?: string | null;
  isLocked?: boolean | null;
}

/**
 * API response containing a single academic year.
 */
export interface AcademicYearResponse {
  success: boolean;
  message: string;
  data: AcademicYear;
}

/**
 * API response containing a list of academic years.
 */
export interface AcademicYearsListResponse {
  success: boolean;
  message: string;
  data: AcademicYear[];
}

/**
 * Paginated API response for academic years.
 */
export interface PaginatedAcademicYearsResponse {
  success: boolean;
  message: string;
  data: {
    items: AcademicYear[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

/**
 * Query parameters for fetching a paginated list of academic years.
 */
export interface AcademicYearsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}
