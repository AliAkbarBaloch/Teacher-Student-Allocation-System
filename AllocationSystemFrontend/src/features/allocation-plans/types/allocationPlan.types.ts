/**
 * AllocationPlan management types
*/

export type PlanStatus = "DRAFT" | "IN_REVIEW" | "APPROVED" | "ARCHIVED";

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

export interface CreateAllocationPlanRequest {
  yearId: number;
  planName: string;
  planVersion: string;
  status: PlanStatus;
  isCurrent?: boolean;
  notes?: string | null;
}

export interface UpdateAllocationPlanRequest {
  planName?: string;
  status?: PlanStatus;
  isCurrent?: boolean;
  notes?: string | null;
}

export interface AllocationPlanResponse {
  success: boolean;
  message: string;
  data: AllocationPlan;
}

export interface AllocationPlansListResponse {
  success: boolean;
  message: string;
  data: AllocationPlan[];
}

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