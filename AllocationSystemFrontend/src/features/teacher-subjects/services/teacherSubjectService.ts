import { apiClient } from "@/lib/api-client";
import type {
  TeacherSubject,
  CreateTeacherSubjectRequest,
  UpdateTeacherSubjectRequest,
  PaginatedTeacherSubjectsResponse,
  TeacherSubjectsListParams,
} from "../types/teacherSubject.types";

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
    if (params.academicYearId !== undefined) {
      queryParams.append("academicYearId", String(params.academicYearId));
    }
    if (params.teacherId !== undefined) {
      queryParams.append("teacherId", String(params.teacherId));
    }
    if (params.subjectId !== undefined) {
      queryParams.append("subjectId", String(params.subjectId));
    }
    if (params.availabilityStatus) {
      queryParams.append("availabilityStatus", params.availabilityStatus);
    }

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