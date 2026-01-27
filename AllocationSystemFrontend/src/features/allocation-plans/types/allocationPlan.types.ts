/**
 * AllocationPlan management types
 */

export type PlanStatus = "DRAFT" | "IN_REVIEW" | "APPROVED" | "ARCHIVED";

/**
 * Represents an allocation plan.
 */
export interface AllocationPlan {
  id: number;
  yearId: number;
  yearName: string;
  planName: string;
  planVersion: string;
  status: PlanStatus;
  statusDisplayName: string;
  createdAt: string; // ISO string
  updatedAt?: string | null;
  isCurrent: boolean;
  notes?: string | null;
}

/**
 * Request payload for creating a new allocation plan.
 */
export interface CreateAllocationPlanRequest {
  yearId: number;
  planName: string;
  planVersion: string;
  status: PlanStatus;
  isCurrent?: boolean;
  notes?: string | null;
}

/**
 * Request payload for updating an existing allocation plan.
 */
export interface UpdateAllocationPlanRequest {
  planName?: string;
  status?: PlanStatus;
  isCurrent?: boolean;
  notes?: string | null;
}

/**
 * API response containing a single allocation plan.
 */
export interface AllocationPlanResponse {
  success: boolean;
  message: string;
  data: AllocationPlan;
}

/**
 * API response containing a list of allocation plans.
 */
export interface AllocationPlansListResponse {
  success: boolean;
  message: string;
  data: AllocationPlan[];
}

/**
 * API response containing paginated allocation plans.
 */
export interface PaginatedAllocationPlansResponse {
  success: boolean;
  message: string;
  data: {
    items: AllocationPlan[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

/**
 * Query parameters for retrieving allocation plans.
 */
export interface AllocationPlansListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
  yearId?: number;
  status?: PlanStatus;
  isCurrent?: boolean;
}
