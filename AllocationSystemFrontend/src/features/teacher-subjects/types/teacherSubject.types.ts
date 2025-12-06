/**
 * TeacherSubject management types
 */

export interface TeacherSubject {
  id: number;
  academicYearId: number;
  academicYearTitle: string;
  teacherId: number;
  teacherTitle: string;
  subjectId: number;
  subjectCode: string;
  subjectTitle: string;
  availabilityStatus: "AVAILABLE" | "NOT_AVAILABLE" | "LIMITED" | "PREFERRED" | string;
  gradeLevelFrom?: number | null;
  gradeLevelTo?: number | null;
  notes?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CreateTeacherSubjectRequest {
  academicYearId: number;
  teacherId: number;
  subjectId: number;
  availabilityStatus: "AVAILABLE" | "NOT_AVAILABLE" | "LIMITED" | "PREFERRED" | string;
  gradeLevelFrom?: number | null;
  gradeLevelTo?: number | null;
  notes?: string | null;
}

export interface UpdateTeacherSubjectRequest {
  availabilityStatus?: "AVAILABLE" | "NOT_AVAILABLE" | "LIMITED" | "PREFERRED" | string;
  gradeLevelFrom?: number | null;
  gradeLevelTo?: number | null;
  notes?: string | null;
}

export interface TeacherSubjectResponse {
  success: boolean;
  message: string;
  data: TeacherSubject;
}

export interface TeacherSubjectsListResponse {
  success: boolean;
  message: string;
  data: TeacherSubject[];
}

export interface PaginatedTeacherSubjectsResponse {
  success: boolean;
  message: string;
  data: {
    items: TeacherSubject[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface TeacherSubjectsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
  academicYearId?: number;
  teacherId?: number;
  subjectId?: number;
  availabilityStatus?: string;
}