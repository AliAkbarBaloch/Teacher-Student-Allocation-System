import { apiClient } from "@/lib/api-client";
import {
  BaseApiService,
  type ApiResponse,
} from "@/services/api/BaseApiService";
import type {
  CreateSchoolRequest,
  PaginatedSchoolsResponse,
  School,
  SchoolsListParams,
  SchoolStatusUpdateRequest,
  UpdateSchoolRequest,
} from "../types/school.types";

export class SchoolServiceClass extends BaseApiService<
  School,
  CreateSchoolRequest,
  UpdateSchoolRequest
> {
  constructor() {
    super("schools");
  }

  // Add any custom methods here, e.g., getPaginated
  async getPaginated(
    params: SchoolsListParams = {}
  ): Promise<PaginatedSchoolsResponse["data"]> {
    const queryParams = this.buildQueryParams({
      ...params,
      searchValue: params.search,
    });

    const response = await apiClient.get<
      ApiResponse<PaginatedSchoolsResponse["data"]>
    >(`/schools/paginate?${queryParams.toString()}`);
    return response.data;
  }

  async list(
    params: SchoolsListParams = {}
  ): Promise<PaginatedSchoolsResponse["data"]> {
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
