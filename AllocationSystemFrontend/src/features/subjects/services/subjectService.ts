import { apiClient } from "@/lib/api-client";
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
  PaginatedSubjectsResponse,
  SubjectsListParams,
} from "../types/subject.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * Subject service for handling subject management operations
 */
export class SubjectService {
  /**
   * Get all subjects
   */
  static async getAll(): Promise<Subject[]> {
    const response = await apiClient.get<ApiResponse<Subject[]>>("/subjects");
    return response.data;
  }

  /**
   * Get paginated subjects
   */
  static async getPaginated(params: SubjectsListParams = {}): Promise<PaginatedSubjectsResponse["data"]> {
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

    const response = await apiClient.get<ApiResponse<PaginatedSubjectsResponse["data"]>>(
      `/subjects/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get subject by ID
   */
  static async getById(id: number): Promise<Subject> {
    const response = await apiClient.get<ApiResponse<Subject>>(`/subjects/${id}`);
    return response.data;
  }

  /**
   * Create a new subject
   */
  static async create(subject: CreateSubjectRequest): Promise<Subject> {
    const response = await apiClient.post<ApiResponse<Subject>>("/subjects", subject);
    return response.data;
  }

  /**
   * Update an existing subject
   */
  static async update(id: number, subject: UpdateSubjectRequest): Promise<Subject> {
    const response = await apiClient.put<ApiResponse<Subject>>(`/subjects/${id}`, subject);
    return response.data;
  }

  /**
   * Delete a subject
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/subjects/${id}`);
  }
}

