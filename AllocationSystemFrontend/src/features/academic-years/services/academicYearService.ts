import { apiClient } from "@/lib/api-client";
import { BaseApiService, type ApiResponse } from "@/services/api/BaseApiService";
import type {
  AcademicYear,
  AcademicYearsListParams,
  CreateAcademicYearRequest,
  PaginatedAcademicYearsResponse,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";

export class AcademicYearServiceClass extends BaseApiService<AcademicYear,CreateAcademicYearRequest,UpdateAcademicYearRequest> {
  constructor() {
    super("academic-years");
  }

  // Add any custom methods here, e.g., getPaginated
  async getPaginated(params: AcademicYearsListParams = {}): Promise<PaginatedAcademicYearsResponse["data"]> {
    const queryParams = new URLSearchParams();

    if (params.page !== undefined) queryParams.append("page", String(params.page));
    if (params.pageSize !== undefined) queryParams.append("pageSize", String(params.pageSize));
    if (params.sortBy) queryParams.append("sortBy", params.sortBy);
    if (params.sortOrder) queryParams.append("sortOrder", params.sortOrder);
    if (params.searchValue) queryParams.append("searchValue", params.searchValue);

    const response = await apiClient.get<ApiResponse<PaginatedAcademicYearsResponse["data"]>>(
      `/academic-years/paginate?${queryParams.toString()}`
    );
    return response.data;
  }
}

export const AcademicYearService = new AcademicYearServiceClass();