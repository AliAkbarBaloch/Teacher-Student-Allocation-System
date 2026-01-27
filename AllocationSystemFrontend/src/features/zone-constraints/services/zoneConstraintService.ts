import { apiClient } from "@/lib/api-client";
import type {
  ZoneConstraint,
  CreateZoneConstraintRequest,
  UpdateZoneConstraintRequest,
  PaginatedZoneConstraintsResponse,
  ZoneConstraintsListParams,
} from "../types/zoneConstraint.types";

import { buildQueryParams } from "@/lib/utils/query-params";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * ZoneConstraint service for handling academic year management operations
 */
export class ZoneConstraintService {
  /**
   * Get all academic years
   */
  static async getAll(): Promise<ZoneConstraint[]> {
    const response = await apiClient.get<ApiResponse<ZoneConstraint[]>>("/zone-constraints");
    return response.data;
  }

  /**
   * Get paginated academic years
   */
  static async getPaginated(params: ZoneConstraintsListParams = {}): Promise<PaginatedZoneConstraintsResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedZoneConstraintsResponse["data"]>>(
      `/zone-constraints/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get academic year by ID
   */
  static async getById(id: number): Promise<ZoneConstraint> {
    const response = await apiClient.get<ApiResponse<ZoneConstraint>>(`/zone-constraints/${id}`);
    return response.data;
  }

  /**
   * Create a new academic year
   */
  static async create(ZoneConstraint: CreateZoneConstraintRequest): Promise<ZoneConstraint> {
    const response = await apiClient.post<ApiResponse<ZoneConstraint>>("/zone-constraints", ZoneConstraint);
    return response.data;
  }

  /**
   * Update an existing academic year
   */
  static async update(id: number, ZoneConstraint: UpdateZoneConstraintRequest): Promise<ZoneConstraint> {
    const response = await apiClient.put<ApiResponse<ZoneConstraint>>(`/zone-constraints/${id}`, ZoneConstraint);
    return response.data;
  }

  /**
   * Delete an academic year
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/zone-constraints/${id}`);
  }
}