import { apiClient } from "@/lib/api-client";
import type {
  TeacherSubject,
  CreateTeacherSubjectRequest,
  UpdateTeacherSubjectRequest,
  PaginatedTeacherSubjectsResponse,
  TeacherSubjectsListParams,
} from "../types/teacherSubject.types";

import { buildQueryParams } from "@/lib/utils/query-params";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * Teacher Subject service for handling teacher subject management operations
 */
export class TeacherSubjectService {
  /**
   * Get all teacher subjects
   */
  static async getAll(): Promise<TeacherSubject[]> {
    const response = await apiClient.get<ApiResponse<TeacherSubject[]>>("/teacher-subjects");
    return response.data;
  }

  /**
   * Get paginated teacher subjects
   */
  static async getPaginated(params: TeacherSubjectsListParams = {}): Promise<PaginatedTeacherSubjectsResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedTeacherSubjectsResponse["data"]>>(
      `/teacher-subjects/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get teacher subject by ID
   */
  static async getById(id: number): Promise<TeacherSubject> {
    const response = await apiClient.get<ApiResponse<TeacherSubject>>(`/teacher-subjects/${id}`);
    return response.data;
  }

  static async getByTeacherId(teacherId: number): Promise<TeacherSubject[]> {
    const response = await apiClient.get<ApiResponse<TeacherSubject[]>>(`/teacher-subjects/by-teacher/${teacherId}`);
    return response.data;
  }

  /**
   * Create a new teacher subject
   */
  static async create(teacherSubject: CreateTeacherSubjectRequest): Promise<TeacherSubject> {
    const response = await apiClient.post<ApiResponse<TeacherSubject>>("/teacher-subjects", teacherSubject);
    return response.data;
  }

  /**
   * Update an existing teacher subject
   */
  static async update(id: number, teacherSubject: UpdateTeacherSubjectRequest): Promise<TeacherSubject> {
    const response = await apiClient.put<ApiResponse<TeacherSubject>>(`/teacher-subjects/${id}`, teacherSubject);
    return response.data;
  }

  /**
   * Delete a teacher subject
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/teacher-subjects/${id}`);
  }
}