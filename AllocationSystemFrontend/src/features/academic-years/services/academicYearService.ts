import { apiClient } from "@/lib/api-client";
import { BaseApiService, type ApiResponse } from "@/services/api/BaseApiService";
import type {
  AcademicYear,
  AcademicYearsListParams,
  CreateAcademicYearRequest,
  PaginatedAcademicYearsResponse,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";

export class AcademicYearServiceClass extends BaseApiService<AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest> {
  constructor() {
    super("academic-years");
  }

  // Add any custom methods here, e.g., getPaginated
  async getPaginated(params: AcademicYearsListParams = {}): Promise<PaginatedAcademicYearsResponse["data"]> {
    const queryParams = this.buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedAcademicYearsResponse["data"]>>(
      `/academic-years/paginate?${queryParams.toString()}`
    );
    return response.data;
  }
}

export const ACADEMIC_YEAR_SERVICE = new AcademicYearServiceClass();