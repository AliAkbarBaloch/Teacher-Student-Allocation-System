/**
 * Subject Category management types
 */

export interface SubjectCategory {
  id: number;
  categoryTitle: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSubjectCategoryRequest {
  categoryTitle: string;
}

export interface UpdateSubjectCategoryRequest {
  categoryTitle?: string;
}

export interface SubjectCategoryResponse {
  success: boolean;
  message: string;
  data: SubjectCategory;
}

export interface SubjectCategoriesListResponse {
  success: boolean;
  message: string;
  data: SubjectCategory[];
}

export interface PaginatedSubjectCategoriesResponse {
  success: boolean;
  message: string;
  data: {
    items: SubjectCategory[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface SubjectCategoriesListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}

