import { apiClient } from "@/lib/api-client";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

export interface RunAllocationResponse {
  planId: number;
  planName: string;
  planVersion: string;
  status: string;
  academicYearId: number;
  academicYearName: string;
  createdAt: string;
}

/**
 * Allocation service for triggering and managing teacher allocation process
 */
export class AllocationService {
  /**
   * Triggers the allocation process for an academic year
   */
  static async runAllocation(
    academicYearId: number,
    isCurrent?: boolean,
    planVersion?: string
  ): Promise<RunAllocationResponse> {
    const response = await apiClient.post<ApiResponse<RunAllocationResponse>>(
      `/allocation/run/${academicYearId}`,
      {
        isCurrent: isCurrent ?? false,
        ...(planVersion && { planVersion }),
      }
    );
    return response.data;
  }
}
