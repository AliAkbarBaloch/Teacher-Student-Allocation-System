import { apiClient } from "@/lib/api-client";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
  PaginatedAcademicYearsResponse,
  AcademicYearsListParams,
} from "../types/academicYear.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * AcademicYear service for handling academic year management operations
 */
export class AcademicYearService {
  /**
   * Get all academic years
   */
  static async getAll(): Promise<AcademicYear[]> {
    const response = await apiClient.get<ApiResponse<AcademicYear[]>>("/academic-years");
    return response.data;
  }

  /**
   * Get paginated academic years
   */
  static async getPaginated(params: AcademicYearsListParams = {}): Promise<PaginatedAcademicYearsResponse["data"]> {
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

    const response = await apiClient.get<ApiResponse<PaginatedAcademicYearsResponse["data"]>>(
      `/academic-years/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get academic year by ID
   */
  static async getById(id: number): Promise<AcademicYear> {
    const response = await apiClient.get<ApiResponse<AcademicYear>>(`/academic-years/${id}`);
    return response.data;
  }

  /**
   * Create a new academic year
   */
  static async create(academicYear: CreateAcademicYearRequest): Promise<AcademicYear> {
    const response = await apiClient.post<ApiResponse<AcademicYear>>("/academic-years", academicYear);
    return response.data;
  }

  /**
   * Update an existing academic year
   */
  static async update(id: number, academicYear: UpdateAcademicYearRequest): Promise<AcademicYear> {
    const response = await apiClient.put<ApiResponse<AcademicYear>>(`/academic-years/${id}`, academicYear);
    return response.data;
  }

  /**
   * Delete an academic year
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/academic-years/${id}`);
  }
}