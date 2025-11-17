import { apiClient } from "@/lib/api-client";
import type {
  AuditLog,
  AuditLogFilters,
  PaginatedAuditLogResponse,
  AuditLogStats,
} from "../types/auditLog.types";
import { resolveUserSearch } from "../utils/userSearch";
import { buildAuditLogQueryParams } from "../utils/queryParams";

/**
 * Audit log service for handling audit log operations
 */
export class AuditLogService {
  /**
   * Get paginated audit logs with filters
   */
  static async getAuditLogs(
    filters: AuditLogFilters = {}
  ): Promise<PaginatedAuditLogResponse> {
    // Handle userSearch: resolve to userId if provided
    const resolvedFilters = { ...filters };
    if (filters.userSearch) {
      const userId = await resolveUserSearch(filters.userSearch);
      if (userId) {
        resolvedFilters.userId = userId;
      }
      // Remove userSearch from filters as it's not a valid query param
      delete resolvedFilters.userSearch;
    }

    const queryParams = buildAuditLogQueryParams(resolvedFilters);
    const response = await apiClient.get<PaginatedAuditLogResponse>(
      `/audit-logs?${queryParams.toString()}`
    );
    return response;
  }

  /**
   * Get audit logs for a specific entity and record
   */
  static async getAuditLogsForEntity(
    entityName: string,
    recordId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedAuditLogResponse> {
    const queryParams = buildAuditLogQueryParams({ page, size });
    const response = await apiClient.get<{ data: PaginatedAuditLogResponse }>(
      `/audit-logs/entity/${entityName}/${recordId}?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get audit logs for a specific user
   */
  static async getAuditLogsForUser(
    userIdentifier: string,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedAuditLogResponse> {
    const queryParams = buildAuditLogQueryParams({ page, size });
    const response = await apiClient.get<{ data: PaginatedAuditLogResponse }>(
      `/audit-logs/user/${userIdentifier}?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get recent audit logs (last 100)
   */
  static async getRecentAuditLogs(): Promise<AuditLog[]> {
    const response = await apiClient.get<{ data: AuditLog[] }>(
      "/audit-logs/recent"
    );
    return response.data;
  }

  /**
   * Get audit log statistics
   */
  static async getStatistics(
    startDate: string,
    endDate: string
  ): Promise<AuditLogStats> {
    const queryParams = buildAuditLogQueryParams({ startDate, endDate });
    const response = await apiClient.get<{ data: AuditLogStats }>(
      `/audit-logs/statistics?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Export audit logs as CSV
   */
  static async exportAuditLogs(
    filters: AuditLogFilters = {},
    maxRecords: number = 1000
  ): Promise<Blob> {
    // Handle userSearch: resolve to userId if provided
    const resolvedFilters = { ...filters };
    if (filters.userSearch) {
      const userId = await resolveUserSearch(filters.userSearch);
      if (userId) {
        resolvedFilters.userId = userId;
      }
      // Remove userSearch from filters as it's not a valid query param
      delete resolvedFilters.userSearch;
    }

    const queryParams = buildAuditLogQueryParams(resolvedFilters);
    queryParams.append("maxRecords", String(maxRecords));

    // Use apiClient.getBlob() for consistent error handling, authentication, and configuration
    return apiClient.getBlob(`/audit-logs/export?${queryParams.toString()}`);
  }
}

