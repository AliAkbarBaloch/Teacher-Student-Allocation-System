import { apiClient } from "@/lib/api-client";
import { BaseApiService, type ApiResponse } from "@/services/api/BaseApiService";
import type {
  CreateSchoolRequest,
  PaginatedSchoolsResponse,
  School,
  SchoolsListParams,
  SchoolStatusUpdateRequest,
  UpdateSchoolRequest,
} from "../types/school.types";

export class SchoolServiceClass extends BaseApiService<School, CreateSchoolRequest, UpdateSchoolRequest> {
  constructor() {
    super("schools");
  }

  // Add any custom methods here, e.g., getPaginated
  async getPaginated(params: SchoolsListParams = {}): Promise<PaginatedSchoolsResponse["data"]> {
    const queryParams = new URLSearchParams();

    if (params.page !== undefined) queryParams.append("page", String(params.page));
    if (params.pageSize !== undefined) queryParams.append("pageSize", String(params.pageSize));
    if (params.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params.sortOrder) queryParams.append("sortOrder", params.sortOrder);
    if (params.searchValue) queryParams.append("searchValue", params.searchValue);
    if (params.isActive !== undefined) queryParams.append("isActive", String(params.isActive));
    if (params.schoolType) queryParams.append("schoolType", params.schoolType);
    if (params.zoneNumber !== undefined) queryParams.append("zoneNumber", String(params.zoneNumber));

    const response = await apiClient.get<ApiResponse<PaginatedSchoolsResponse["data"]>>(
      `/schools/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  async list(params: SchoolsListParams = {}): Promise<PaginatedSchoolsResponse["data"]> {
    return this.getPaginated(params);
  }

  async updateStatus(id: number, isActive: boolean): Promise<School> {
    const body: SchoolStatusUpdateRequest = { isActive };
    const response = await apiClient.patch<ApiResponse<School>>(
      `/schools/${id}/status`,
      body
      );
    return response.data;
  }

}

export const SchoolService = new SchoolServiceClass();