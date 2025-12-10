import { apiClient } from "@/lib/api-client";
import type {
  TeacherFormSubmission,
  PaginatedTeacherFormSubmissionResponse,
  TeacherFormSubmissionListParams,
  UpdateSubmissionStatusRequest,
  TeacherFormSubmissionApiResponse,
  PaginatedTeacherFormSubmissionApiResponse,
  FormLinkGenerateRequest,
  FormLinkResponse,
  FormLinkApiResponse,
  PublicFormSubmissionRequest,
} from "../types/teacherFormSubmission.types";

/**
 * Service for handling teacher form submission operations
 */
export class TeacherFormSubmissionService {
  /**
   * Get paginated teacher form submissions with filters
   */
  static async list(params: TeacherFormSubmissionListParams = {}): Promise<PaginatedTeacherFormSubmissionResponse> {
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
    if (params.teacherId !== undefined) {
      queryParams.append("teacherId", String(params.teacherId));
    }
    if (params.yearId !== undefined) {
      queryParams.append("yearId", String(params.yearId));
    }
    if (params.isProcessed !== undefined) {
      queryParams.append("isProcessed", String(params.isProcessed));
    }

    const response = await apiClient.get<PaginatedTeacherFormSubmissionApiResponse>(
      `/teacher-form-submissions?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get teacher form submission by ID
   */
  static async getById(id: number): Promise<TeacherFormSubmission> {
    const response = await apiClient.get<TeacherFormSubmissionApiResponse>(
      `/teacher-form-submissions/${id}`
    );
    return response.data;
  }

  /**
   * Update the processing status of a form submission
   */
  static async updateStatus(
    id: number,
    status: UpdateSubmissionStatusRequest
  ): Promise<TeacherFormSubmission> {
    const response = await apiClient.patch<TeacherFormSubmissionApiResponse>(
      `/teacher-form-submissions/${id}/status`,
      status
    );
    return response.data;
  }

  /**
   * Generate a form link for a teacher
   */
  static async generateFormLink(request: FormLinkGenerateRequest): Promise<FormLinkResponse> {
    const response = await apiClient.post<FormLinkApiResponse>(
      "/teacher-form-submissions/generate-link",
      request
    );
    return response.data;
  }

  /**
   * Get form details by token (public endpoint)
   */
  static async getFormDetailsByToken(token: string): Promise<FormLinkResponse> {
    const response = await apiClient.get<FormLinkApiResponse>(
      `/public/teacher-form-submission/${token}`
    );
    return response.data;
  }

  /**
   * Submit form by token (public endpoint)
   */
  static async submitFormByToken(
    token: string,
    submission: PublicFormSubmissionRequest
  ): Promise<TeacherFormSubmission> {
    const response = await apiClient.post<TeacherFormSubmissionApiResponse>(
      `/public/teacher-form-submission/${token}`,
      submission
    );
    return response.data;
  }
}

