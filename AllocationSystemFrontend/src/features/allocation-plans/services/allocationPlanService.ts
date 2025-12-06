import { apiClient } from "@/lib/api-client";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
  PaginatedAllocationPlansResponse,
  AllocationPlansListParams,
} from "../types/allocationPlan.types";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

/**
 * AllocationPlan service for handling allocation plan management operations
 */
export class AllocationPlanService {
  /**
   * Get all allocation plans
   */
  static async getAll(): Promise<AllocationPlan[]> {
    const response = await apiClient.get<ApiResponse<AllocationPlan[]>>("/allocation-plans");
    return response.data;
  }

  /**
   * Get paginated allocation plans
   */
  static async getPaginated(params: AllocationPlansListParams = {}): Promise<PaginatedAllocationPlansResponse["data"]> {
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
    if (params.yearId !== undefined) {
      queryParams.append("yearId", String(params.yearId));
    }
    if (params.status) {
      queryParams.append("status", params.status);
    }
    if (params.isCurrent !== undefined) {
      queryParams.append("isCurrent", String(params.isCurrent));
    }

    const response = await apiClient.get<ApiResponse<PaginatedAllocationPlansResponse["data"]>>(
      `/allocation-plans/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get allocation plan by ID
   */
  static async getById(id: number): Promise<AllocationPlan> {
    const response = await apiClient.get<ApiResponse<AllocationPlan>>(`/allocation-plans/${id}`);
    return response.data;
  }

  /**
   * Create a new allocation plan
   */
  static async create(allocationPlan: CreateAllocationPlanRequest): Promise<AllocationPlan> {
    const response = await apiClient.post<ApiResponse<AllocationPlan>>("/allocation-plans", allocationPlan);
    return response.data;
  }

  /**
   * Update an existing allocation plan
   */
  static async update(id: number, allocationPlan: UpdateAllocationPlanRequest): Promise<AllocationPlan> {
    const response = await apiClient.put<ApiResponse<AllocationPlan>>(`/allocation-plans/${id}`, allocationPlan);
    return response.data;
  }

  /**
   * Delete an allocation plan
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/allocation-plans/${id}`);
  }
}