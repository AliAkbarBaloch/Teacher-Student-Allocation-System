import { apiClient } from "@/lib/api-client";
import type {
  CreditHourTracking,
  PaginatedCreditHourTrackingResponse,
  CreditHourTrackingListParams,
  CreateCreditHourTrackingRequest,
  UpdateCreditHourTrackingRequest,
  CreditHourTrackingResponse,
  CreditHourTrackingListResponse,
  SortField,
} from "../types/creditHourTracking.types";

import { buildQueryParams } from "@/lib/utils/query-params";

/**
 * Service for handling credit hour tracking operations
 */
export class CreditHourTrackingService {
  /**
   * Get paginated credit hour tracking entries with filters
   */
  static async list(params: CreditHourTrackingListParams = {}): Promise<PaginatedCreditHourTrackingResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<PaginatedCreditHourTrackingResponse>(
      `/credit-hour-tracking/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get all credit hour tracking entries without pagination
   */
  static async getAll(): Promise<CreditHourTracking[]> {
    const response = await apiClient.get<CreditHourTrackingListResponse>(
      "/credit-hour-tracking"
    );
    return response.data;
  }

  /**
   * Get credit hour tracking entry by ID
   */
  static async getById(id: number): Promise<CreditHourTracking> {
    const response = await apiClient.get<CreditHourTrackingResponse>(
      `/credit-hour-tracking/${id}`
    );
    return response.data;
  }

  /**
   * Create a new credit hour tracking entry
   */
  static async create(data: CreateCreditHourTrackingRequest): Promise<CreditHourTracking> {
    const response = await apiClient.post<CreditHourTrackingResponse>(
      "/credit-hour-tracking",
      data
    );
    return response.data;
  }

  /**
   * Update a credit hour tracking entry
   */
  static async update(
    id: number,
    data: UpdateCreditHourTrackingRequest
  ): Promise<CreditHourTracking> {
    const response = await apiClient.put<CreditHourTrackingResponse>(
      `/credit-hour-tracking/${id}`,
      data
    );
    return response.data;
  }

  /**
   * Delete a credit hour tracking entry
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/credit-hour-tracking/${id}`);
  }

  /**
   * Get available sort fields
   */
  static async getSortFields(): Promise<SortField[]> {
    const response = await apiClient.get<{ success: boolean; data: SortField[] }>(
      "/credit-hour-tracking/sort-fields"
    );
    return response.data;
  }
}
