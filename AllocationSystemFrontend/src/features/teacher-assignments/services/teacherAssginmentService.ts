import { apiClient } from "@/lib/api-client";
import type {
  TeacherAssignment,
  CreateTeacherAssignmentRequest,
  UpdateTeacherAssignmentRequest,
  PaginatedTeacherAssignmentsResponse,
  TeacherAssignmentsListParams,
} from "../types/teacherAssignment.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * teacherAssignment service for handling teacherAssignment management operations
 */
export class TeacherAssignmentService {
  /**
   * Get all teacherAssignments
   */
  static async getAll(): Promise<TeacherAssignment[]> {
    const response = await apiClient.get<ApiResponse<TeacherAssignment[]>>("/teacher-assignments");
    return response.data;
  }

  /**
   * Get paginated teacherAssignments
   */
  static async getPaginated(params: TeacherAssignmentsListParams = {}): Promise<PaginatedTeacherAssignmentsResponse["data"]> {
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

    const response = await apiClient.get<ApiResponse<PaginatedTeacherAssignmentsResponse["data"]>>(
      `/teacher-assignments/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get teacherAssignment by ID
   */
  static async getById(id: number): Promise<TeacherAssignment> {
    const response = await apiClient.get<ApiResponse<TeacherAssignment>>(`/teacher-assignments/${id}`);
    return response.data;
  }

  /**
   * Create a new teacherAssignment
   */
  static async create(teacherAssignment: CreateTeacherAssignmentRequest): Promise<TeacherAssignment> {
    const response = await apiClient.post<ApiResponse<TeacherAssignment>>("/teacher-assignments", teacherAssignment);
    return response.data;
  }

  /**
   * Update an existing teacherAssignment
   */
  static async update(id: number, teacherAssignment: UpdateTeacherAssignmentRequest): Promise<TeacherAssignment> {
    const response = await apiClient.put<ApiResponse<TeacherAssignment>>(`/teacher-assignments/${id}`, teacherAssignment);
    return response.data;
  }

  /**
   * Delete a teacherAssignment
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/teacher-assignments/${id}`);
  }
}

