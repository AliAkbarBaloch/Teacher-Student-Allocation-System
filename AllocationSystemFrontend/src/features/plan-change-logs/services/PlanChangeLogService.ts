import { apiClient } from "@/lib/api-client";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
  PaginatedPlanChangeLogsResponse,
  PlanChangeLogsListParams,
} from "../types/planChangeLog.types";

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
    if (params.changeType) {
      queryParams.append("changeType", params.changeType);
    }
    if (params.entityType) {
      queryParams.append("entityType", params.entityType);
    }
    if (params.startDate) {
      queryParams.append("startDate", params.startDate);
    }
    if (params.endDate) {
      queryParams.append("endDate", params.endDate);
    }
    if (params.planId !== undefined) {
      queryParams.append("planId", String(params.planId));
    }

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