import { apiClient } from "@/lib/api-client";
import type {
  Teacher,
  PaginatedTeacherResponse,
  TeacherListParams,
  CreateTeacherRequest,
  UpdateTeacherRequest,
  TeacherStatusUpdateRequest,
  BulkImportResponse,
} from "../types/teacher.types";

type ApiResponse<T> = {
  data: T;
};

export class TeacherService {
  static async list(params: TeacherListParams = {}): Promise<PaginatedTeacherResponse> {
    const query = new URLSearchParams();

    if (params.page) query.set("page", String(params.page));
    if (params.pageSize) query.set("pageSize", String(params.pageSize));
    if (params.sortBy) query.set("sortBy", params.sortBy);
    if (params.sortOrder) query.set("sortOrder", params.sortOrder);
    if (params.search) query.set("searchValue", params.search);
    if (params.schoolId) query.set("schoolId", String(params.schoolId));
    if (params.employmentStatus) query.set("employmentStatus", params.employmentStatus);
    if (typeof params.isActive === "boolean") query.set("isActive", String(params.isActive));

    const response = await apiClient.get<ApiResponse<PaginatedTeacherResponse>>(
      `/teachers/paginate${query.toString() ? `?${query.toString()}` : ""}`
    );

    return response.data;
  }

  static async getAll(): Promise<Teacher[]> {
    const response = await apiClient.get<ApiResponse<Teacher[]>>("/teachers");
    return response.data;
  }

  static async getById(id: number): Promise<Teacher> {
    const response = await apiClient.get<ApiResponse<Teacher>>(`/teachers/${id}`);
    return response.data;
  }

  static async create(payload: CreateTeacherRequest): Promise<Teacher> {
    const response = await apiClient.post<ApiResponse<Teacher>>("/teachers", payload);
    return response.data;
  }

  static async update(id: number, payload: UpdateTeacherRequest): Promise<Teacher> {
    const response = await apiClient.put<ApiResponse<Teacher>>(`/teachers/${id}`, payload);
    return response.data;
  }

  static async updateStatus(id: number, isActive: boolean): Promise<Teacher> {
    const body: TeacherStatusUpdateRequest = { isActive };
    const response = await apiClient.patch<ApiResponse<Teacher>>(`/teachers/${id}/status`, body);
    return response.data;
  }

  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/teachers/${id}`);
  }

  static async bulkImport(
    file: File,
    skipInvalidRows: boolean = false
  ): Promise<BulkImportResponse> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("skipInvalidRows", String(skipInvalidRows));

    // Don't set Content-Type manually - let the browser set it with boundary for FormData
    // The apiClient automatically detects FormData and omits JSON content type
    const response = await apiClient.post<ApiResponse<BulkImportResponse>>(
      "/teachers/bulk-import",
      formData
    );
    return response.data;
  }

  /**
   * Check which emails already exist in the database
   * @param emails Array of email addresses to check
   * @returns Set of emails that already exist
   */
  static async checkExistingEmails(emails: string[]): Promise<Set<string>> {
    if (emails.length === 0) return new Set();
    
    try {
      const response = await apiClient.post<ApiResponse<string[]>>(
        "/teachers/check-emails",
        emails
      );
      return new Set(response.data.map(email => email.toLowerCase()));
    } catch (error) {
      // If checking fails, return empty set - backend will catch duplicates anyway
      console.warn("Failed to check existing emails:", error);
      return new Set();
    }
  }
}

