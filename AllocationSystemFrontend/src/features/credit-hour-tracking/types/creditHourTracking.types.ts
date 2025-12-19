/**
 * Credit Hour Tracking management types
 */

export interface CreditHourTracking {
  id: number;
  teacherId: number;
  teacherName: string;
  academicYearId: number;
  academicYearTitle: string;
  assignmentsCount: number;
  creditHoursAllocated: number;
  creditBalance: number;
  notes?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CreateCreditHourTrackingRequest {
  teacherId: number;
  academicYearId: number;
  assignmentsCount: number;
  creditHoursAllocated: number;
  creditBalance?: number;
  notes?: string | null;
}

export interface UpdateCreditHourTrackingRequest {
  teacherId?: number;
  academicYearId?: number;
  assignmentsCount?: number;
  creditHoursAllocated?: number;
  creditBalance?: number;
  notes?: string | null;
}

export interface CreditHourTrackingResponse {
  success: boolean;
  message: string;
  data: CreditHourTracking;
}

export interface CreditHourTrackingListResponse {
  success: boolean;
  message: string;
  data: CreditHourTracking[];
}

export interface PaginatedCreditHourTrackingResponse {
  success: boolean;
  message: string;
  data: {
    items: CreditHourTracking[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface CreditHourTrackingListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
  academicYearId?: number;
  teacherId?: number;
  minBalance?: number;
}

export interface SortField {
  key: string;
  label: string;
}
