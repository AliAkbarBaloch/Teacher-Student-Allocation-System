import type { AuditLogFilters } from "../types/auditLog.types";

/**
 * Builds URLSearchParams from audit log filters
 * @param filters - Audit log filters object
 * @param additionalParams - Additional query parameters to include
 * @returns URLSearchParams object
 */
export function buildAuditLogQueryParams(
  filters: AuditLogFilters,
  additionalParams?: Record<string, string | number>
): URLSearchParams {
  const queryParams = new URLSearchParams();

  if (filters.userId !== undefined) {
    queryParams.append("userId", String(filters.userId));
  }

  if (filters.action) {
    queryParams.append("action", filters.action);
  }

  if (filters.targetEntity) {
    queryParams.append("targetEntity", filters.targetEntity);
  }

  if (filters.startDate) {
    queryParams.append("startDate", filters.startDate);
  }

  if (filters.endDate) {
    queryParams.append("endDate", filters.endDate);
  }

  if (filters.page !== undefined) {
    queryParams.append("page", String(filters.page));
  }

  if (filters.size !== undefined) {
    queryParams.append("size", String(filters.size));
  }

  if (filters.sortBy) {
    queryParams.append("sortBy", filters.sortBy);
  }

  if (filters.sortDirection) {
    queryParams.append("sortDirection", filters.sortDirection);
  }

  // Add additional parameters
  if (additionalParams) {
    Object.entries(additionalParams).forEach(([key, value]) => {
      queryParams.append(key, String(value));
    });
  }

  return queryParams;
}

