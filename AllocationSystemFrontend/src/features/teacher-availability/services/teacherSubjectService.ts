import { apiClient } from "@/lib/api-client";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
  PaginatedTeacherAvailabilityResponse,
  TeacherAvailabilityListParams,
} from "../types/teacherAvailability.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * TeacherAvailability service for handling teacher availability management operations
 */
export class TeacherAvailabilityService {
  /**
   * Get all teacher availabilities
   */
  static async getAll(): Promise<TeacherAvailability[]> {
    const response = await apiClient.get<ApiResponse<TeacherAvailability[]>>("/teacher-availability");
    return response.data;
  }

  /**
   * Get paginated teacher availabilities
   */
  static async getPaginated(params: TeacherAvailabilityListParams = {}): Promise<PaginatedTeacherAvailabilityResponse["data"]> {
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
    if (params.teacherId !== undefined) {
      queryParams.append("teacherId", String(params.teacherId));
    }
    if (params.academicYearId !== undefined) {
      queryParams.append("academicYearId", String(params.academicYearId));
    }
    if (params.internshipTypeId !== undefined) {
      queryParams.append("internshipTypeId", String(params.internshipTypeId));
    }
    if (params.isAvailable !== undefined) {
      queryParams.append("isAvailable", String(params.isAvailable));
    }

    const response = await apiClient.get<ApiResponse<PaginatedTeacherAvailabilityResponse["data"]>>(
      `/teacher-availability/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get teacher availability by ID
   */
  static async getById(id: number): Promise<TeacherAvailability> {
    const response = await apiClient.get<ApiResponse<TeacherAvailability>>(`/teacher-availability/${id}`);
    return response.data;
  }

  /**
   * Create a new teacher availability
   */
  static async create(availability: CreateTeacherAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await apiClient.post<ApiResponse<TeacherAvailability>>("/teacher-availability", availability);
    return response.data;
  }

  /**
   * Update an existing teacher availability
   */
  static async update(id: number, availability: UpdateTeacherAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await apiClient.put<ApiResponse<TeacherAvailability>>(`/teacher-availability/${id}`, availability);
    return response.data;
  }

  /**
   * Delete a teacher availability
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/teacher-availability/${id}`);
  }
}