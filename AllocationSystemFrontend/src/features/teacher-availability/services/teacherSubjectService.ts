import { apiClient } from "@/lib/api-client";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
  PaginatedTeacherAvailabilityResponse,
  TeacherAvailabilityListParams,
} from "../types/teacherAvailability.types";

import { buildQueryParams } from "@/lib/utils/query-params";

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
    const queryParams = buildQueryParams(params);

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