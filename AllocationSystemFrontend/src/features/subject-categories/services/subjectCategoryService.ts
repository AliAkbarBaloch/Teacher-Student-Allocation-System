import { apiClient } from "@/lib/api-client";
import type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
  PaginatedSubjectCategoriesResponse,
  SubjectCategoriesListParams,
} from "../types/subjectCategory.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * Subject Category service for handling subject category management operations
 */
export class SubjectCategoryService {
  /**
   * Get all subject categories
   */
  static async getAll(): Promise<SubjectCategory[]> {
    const response = await apiClient.get<ApiResponse<SubjectCategory[]>>("/subject-categories");
    return response.data;
  }

  /**
   * Get paginated subject categories
   */
  static async getPaginated(params: SubjectCategoriesListParams = {}): Promise<PaginatedSubjectCategoriesResponse["data"]> {
    const queryParams = new URLSearchParams();
    
    if (params.page !== undefined) {
      queryParams.append("page", String(params.page));
    }
    if (params.pageSize !== undefined) {
      queryParams.append("pageSize", String(params.pageSize));
    }
    if (params.sortBy) {
      queryParams.append("sortBy", params.sortBy);
    }
    if (params.sortOrder) {
      queryParams.append("sortOrder", params.sortOrder);
    }
    if (params.searchValue) {
      queryParams.append("searchValue", params.searchValue);
    }

    const response = await apiClient.get<ApiResponse<PaginatedSubjectCategoriesResponse["data"]>>(
      `/subject-categories/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get subject category by ID
   */
  static async getById(id: number): Promise<SubjectCategory> {
    const response = await apiClient.get<ApiResponse<SubjectCategory>>(`/subject-categories/${id}`);
    return response.data;
  }

  /**
   * Create a new subject category
   */
  static async create(category: CreateSubjectCategoryRequest): Promise<SubjectCategory> {
    const response = await apiClient.post<ApiResponse<SubjectCategory>>("/subject-categories", category);
    return response.data;
  }

  /**
   * Update an existing subject category
   */
  static async update(id: number, category: UpdateSubjectCategoryRequest): Promise<SubjectCategory> {
    const response = await apiClient.put<ApiResponse<SubjectCategory>>(`/subject-categories/${id}`, category);
    return response.data;
  }

  /**
   * Delete a subject category
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/subject-categories/${id}`);
  }
}

