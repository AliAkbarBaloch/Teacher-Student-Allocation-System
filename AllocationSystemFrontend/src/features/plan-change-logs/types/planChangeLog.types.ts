/**
 * PlanChangeLog management types
 */

export interface PlanChangeLog {
  id: number;
  planId: number;
  changeType: string;
  entityType: string;
  entityId: number;
  oldValue?: string | null;
  newValue?: string | null;
  reason?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CreatePlanChangeLogRequest {
  planId: number;
  changeType: string;
  entityType: string;
  entityId: number;
  oldValue?: string | null;
  newValue?: string | null;
  reason?: string | null;
}

export interface UpdatePlanChangeLogRequest {
  changeType?: string;
  entityType?: string;
  entityId?: number;
  oldValue?: string | null;
  newValue?: string | null;
  reason?: string | null;
}

export interface PlanChangeLogResponse {
  success: boolean;
  message: string;
  data: PlanChangeLog;
}

export interface PlanChangeLogsListResponse {
  success: boolean;
  message: string;
  data: PlanChangeLog[];
}

export interface PaginatedPlanChangeLogsResponse {
  success: boolean;
  message: string;
  data: {
    items: PlanChangeLog[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface PlanChangeLogsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  changeType?: string;
  entityType?: string;
  startDate?: string;
  endDate?: string;
  planId?: number;
}