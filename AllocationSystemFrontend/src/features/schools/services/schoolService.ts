import { apiClient } from "@/lib/api-client";
import type {
  CreateSchoolRequest,
  PaginatedSchoolResponse,
  School,
  SchoolListParams,
  SchoolStatusUpdateRequest,
  UpdateSchoolRequest,
} from "../types/school.types";

type ApiResponse<T> = {
  data: T;
};

export class SchoolService {
  static async list(params: SchoolListParams = {}): Promise<PaginatedSchoolResponse> {
    const query = new URLSearchParams();

    if (params.page) query.set("page", String(params.page));
    if (params.pageSize) query.set("pageSize", String(params.pageSize));
    if (params.sortBy) query.set("sortBy", params.sortBy);
    if (params.sortOrder) query.set("sortOrder", params.sortOrder);
    if (params.search) query.set("searchValue", params.search);
    if (params.schoolType) query.set("schoolType", params.schoolType);
    if (typeof params.zoneNumber === "number") query.set("zoneNumber", String(params.zoneNumber));
    if (typeof params.isActive === "boolean") query.set("isActive", String(params.isActive));

    const response = await apiClient.get<ApiResponse<PaginatedSchoolResponse>>(
      `/schools/paginate${query.toString() ? `?${query.toString()}` : ""}`
    );

    return response.data;
  }

  static async getById(id: number): Promise<School> {
    const response = await apiClient.get<ApiResponse<School>>(`/schools/${id}`);
    return response.data;
  }

  static async create(payload: CreateSchoolRequest): Promise<School> {
    const response = await apiClient.post<ApiResponse<School>>("/schools", payload);
    return response.data;
  }

  static async update(id: number, payload: UpdateSchoolRequest): Promise<School> {
    const response = await apiClient.put<ApiResponse<School>>(`/schools/${id}`, payload);
    return response.data;
  }

  static async updateStatus(id: number, isActive: boolean): Promise<School> {
    const body: SchoolStatusUpdateRequest = { isActive };
    const response = await apiClient.patch<ApiResponse<School>>(
      `/schools/${id}/status`,
      body
    );
    return response.data;
  }

  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/schools/${id}`);
  }
}

