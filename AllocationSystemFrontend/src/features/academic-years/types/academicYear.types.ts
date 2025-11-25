/**
 * AcademicYear management types
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

export interface CreateAcademicYearRequest {
  yearName: string;
  totalCreditHours: number;
  elementarySchoolHours: number;
  middleSchoolHours: number;
  budgetAnnouncementDate: string;
  allocationDeadline?: string | null;
  isLocked?: boolean | null;
}

export interface UpdateAcademicYearRequest {
  yearName?: string;
  totalCreditHours?: number;
  elementarySchoolHours?: number;
  middleSchoolHours?: number;
  budgetAnnouncementDate?: string;
  allocationDeadline?: string | null;
  isLocked?: boolean | null;
}

export interface AcademicYearResponse {
  success: boolean;
  message: string;
  data: AcademicYear;
}

export interface AcademicYearsListResponse {
  success: boolean;
  message: string;
  data: AcademicYear[];
}

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

export interface AcademicYearsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}