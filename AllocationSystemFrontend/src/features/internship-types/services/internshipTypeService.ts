import { apiClient } from "@/lib/api-client";
import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
  PaginatedInternshipTypesResponse,
  InternshipTypesListParams,
} from "../types/internshipType.types";

import { buildQueryParams } from "@/lib/utils/query-params";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * Internship Type service for handling internship type management operations
 */
export class InternshipTypeService {
  /**
   * Get all internship types
   */
  static async getAll(): Promise<InternshipType[]> {
    const response = await apiClient.get<ApiResponse<InternshipType[]>>("/internship-types");
    return response.data;
  }

  /**
   * Get paginated internship types
   */
  static async getPaginated(params: InternshipTypesListParams = {}): Promise<PaginatedInternshipTypesResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedInternshipTypesResponse["data"]>>(
      `/internship-types/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get internship type by ID
   */
  static async getById(id: number): Promise<InternshipType> {
    const response = await apiClient.get<ApiResponse<InternshipType>>(`/internship-types/${id}`);
    return response.data;
  }

  /**
   * Create a new internship type
   */
  static async create(internshipType: CreateInternshipTypeRequest): Promise<InternshipType> {
    const response = await apiClient.post<ApiResponse<InternshipType>>("/internship-types", internshipType);
    return response.data;
  }

  /**
   * Update an existing internship type
   */
  static async update(id: number, internshipType: UpdateInternshipTypeRequest): Promise<InternshipType> {
    const response = await apiClient.put<ApiResponse<InternshipType>>(`/internship-types/${id}`, internshipType);
    return response.data;
  }

  /**
   * Delete an internship type
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/internship-types/${id}`);
  }
}

