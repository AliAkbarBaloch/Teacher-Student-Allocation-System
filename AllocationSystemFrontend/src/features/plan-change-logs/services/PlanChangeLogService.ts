import { apiClient } from "@/lib/api-client";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
  PaginatedPlanChangeLogsResponse,
  PlanChangeLogsListParams,
} from "../types/planChangeLog.types";

import { buildQueryParams } from "@/lib/utils/query-params";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * PlanChangeLog service for handling plan change log management operations
 */
export class PlanChangeLogService {
  /**
   * Get all plan change logs
   */
  static async getAll(): Promise<PlanChangeLog[]> {
    const response = await apiClient.get<ApiResponse<PlanChangeLog[]>>("/plan-change-logs");
    return response.data;
  }

  /**
   * Get paginated plan change logs
   */
  static async getPaginated(params: PlanChangeLogsListParams = {}): Promise<PaginatedPlanChangeLogsResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<ApiResponse<PaginatedPlanChangeLogsResponse["data"]>>(
      `/plan-change-logs/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get plan change log by ID
   */
  static async getById(id: number): Promise<PlanChangeLog> {
    const response = await apiClient.get<ApiResponse<PlanChangeLog>>(`/plan-change-logs/${id}`);
    return response.data;
  }

  /**
   * Create a new plan change log
   */
  static async create(log: CreatePlanChangeLogRequest): Promise<PlanChangeLog> {
    const response = await apiClient.post<ApiResponse<PlanChangeLog>>("/plan-change-logs", log);
    return response.data;
  }

  /**
   * Update an existing plan change log
   */
  static async update(id: number, log: UpdatePlanChangeLogRequest): Promise<PlanChangeLog> {
    const response = await apiClient.put<ApiResponse<PlanChangeLog>>(`/plan-change-logs/${id}`, log);
    return response.data;
  }

  /**
   * Delete a plan change log
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/plan-change-logs/${id}`);
  }
}