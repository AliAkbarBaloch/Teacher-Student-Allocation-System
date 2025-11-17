import type { AuditLogFilters } from "../types/auditLog.types";

/**
 * Default audit log filter values
 */
export const DEFAULT_AUDIT_LOG_FILTERS: AuditLogFilters = {
  page: 0,
  size: 20,
  sortBy: "eventTimestamp",
  sortDirection: "DESC",
} as const;

/**
 * Page size options for pagination
 */
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const;

/**
 * Default page size
 */
export const DEFAULT_PAGE_SIZE = 20;

