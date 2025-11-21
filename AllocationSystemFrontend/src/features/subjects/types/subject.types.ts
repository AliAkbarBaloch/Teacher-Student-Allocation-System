/**
 * Subject management types
 */

export interface Subject {
  id: number;
  subjectCode: string;
  subjectTitle: string;
  subjectCategoryId: number;
  subjectCategoryTitle: string;
  schoolType: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSubjectRequest {
  subjectCode: string;
  subjectTitle: string;
  subjectCategoryId: number;
  schoolType?: string | null;
  isActive?: boolean;
}

export interface UpdateSubjectRequest {
  subjectCode?: string;
  subjectTitle?: string;
  subjectCategoryId?: number;
  schoolType?: string | null;
  isActive?: boolean;
}

export interface SubjectResponse {
  success: boolean;
  message: string;
  data: Subject;
}

export interface SubjectsListResponse {
  success: boolean;
  message: string;
  data: Subject[];
}

export interface PaginatedSubjectsResponse {
  success: boolean;
  message: string;
  data: {
    items: Subject[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface SubjectsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}

