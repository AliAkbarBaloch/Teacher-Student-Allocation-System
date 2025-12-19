/**
 * TeacherAvailability management types
 */

export type AvailabilityStatus = "AVAILABLE" | "PREFERRED" | "NOT_AVAILABLE" | "BACKUP_ONLY";

export interface TeacherAvailability {
  id: number;
  teacherId: number;
  teacherFirstName: string;
  teacherLastName: string;
  academicYearId: number;
  academicYearName: string;
  internshipTypeId: number;
  internshipTypeName: string;
  status: AvailabilityStatus;
  isAvailable: boolean;
  preferenceRank?: number | null;
  notes?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CreateTeacherAvailabilityRequest {
  teacherId: number;
  academicYearId: number;
  internshipTypeId: number;
  status: AvailabilityStatus;
  isAvailable?: boolean;
  preferenceRank?: number | null;
  notes?: string | null;
}

export interface UpdateTeacherAvailabilityRequest {
  status?: AvailabilityStatus;
  isAvailable?: boolean;
  preferenceRank?: number | null;
  notes?: string | null;
}

export interface TeacherAvailabilityResponse {
  success: boolean;
  message: string;
  data: TeacherAvailability;
}

export interface TeacherAvailabilityListResponse {
  success: boolean;
  message: string;
  data: TeacherAvailability[];
}

export interface PaginatedTeacherAvailabilityResponse {
  success: boolean;
  message: string;
  data: {
    items: TeacherAvailability[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface TeacherAvailabilityListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
  teacherId?: number;
  academicYearId?: number;
  internshipTypeId?: number;
  isAvailable?: boolean;
}