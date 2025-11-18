import { DEFAULT_TABLE_PAGE_SIZE, TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import type { AuditLogFilters } from "../types/auditLog.types";

/**
 * Default audit log filter values
 */
export const DEFAULT_AUDIT_LOG_FILTERS: AuditLogFilters = {
  page: 0,
  size: DEFAULT_TABLE_PAGE_SIZE,
  sortBy: "eventTimestamp",
  sortDirection: "DESC",
} as const;

/**
 * Page size options for pagination
 */
export const PAGE_SIZE_OPTIONS = TABLE_PAGE_SIZE_OPTIONS;

/**
 * Default page size
 */
export const DEFAULT_PAGE_SIZE = DEFAULT_TABLE_PAGE_SIZE;

