import type { AuditLogFilters } from "../types/auditLog.types";
import { DEFAULT_AUDIT_LOG_FILTERS } from "../constants/filters";
import { AuditAction } from "../types/auditLog.types";
import { formatDateForInput, parseDateInput } from "@/lib/utils/date";

/**
 * Parse URL search params into AuditLogFilters
 * @param searchParams - URLSearchParams from the URL
 * @returns AuditLogFilters object
 */
export function parseFiltersFromUrl(searchParams: URLSearchParams): AuditLogFilters {
  const filters: AuditLogFilters = { ...DEFAULT_AUDIT_LOG_FILTERS };

  // Parse userId
  const userId = searchParams.get("userId");
  if (userId) {
    const parsedUserId = Number.parseInt(userId, 10);
    if (!Number.isNaN(parsedUserId)) {
      filters.userId = parsedUserId;
    }
  }

  // Parse userSearch (for display purposes, not sent to API)
  const userSearch = searchParams.get("userSearch");
  if (userSearch) {
    filters.userSearch = userSearch;
  }

  // Parse action
  const action = searchParams.get("action");
  if (action && Object.values(AuditAction).includes(action as AuditAction)) {
    filters.action = action as AuditAction;
  }

  // Parse targetEntity
  const targetEntity = searchParams.get("targetEntity");
  if (targetEntity) {
    filters.targetEntity = targetEntity;
  }

  // Parse startDate
  // URL stores datetime-local format (YYYY-MM-DDTHH:mm), convert to ISO for filters
  const startDate = searchParams.get("startDate");
  if (startDate) {
    // datetime-local format is exactly 16 chars: "YYYY-MM-DDTHH:mm"
    // ISO format is longer (has seconds/milliseconds/timezone)
    // Check if it's ISO by looking for 'Z', '+' after position 16, or length > 16
    const isIsoFormat = startDate.includes("Z") || 
                        (startDate.includes("+") && startDate.indexOf("+") >= 16) ||
                        startDate.length > 16;
    
    if (isIsoFormat) {
      // Already in ISO format (backward compatibility for old URLs)
      filters.startDate = startDate;
    } else {
      // datetime-local format (16 chars), convert to ISO
      filters.startDate = parseDateInput(startDate);
    }
  }

  // Parse endDate
  // URL stores datetime-local format (YYYY-MM-DDTHH:mm), convert to ISO for filters
  const endDate = searchParams.get("endDate");
  if (endDate) {
    // datetime-local format is exactly 16 chars: "YYYY-MM-DDTHH:mm"
    // ISO format is longer (has seconds/milliseconds/timezone)
    // Check if it's ISO by looking for 'Z', '+' after position 16, or length > 16
    const isIsoFormat = endDate.includes("Z") || 
                        (endDate.includes("+") && endDate.indexOf("+") >= 16) ||
                        endDate.length > 16;
    
    if (isIsoFormat) {
      // Already in ISO format (backward compatibility for old URLs)
      filters.endDate = endDate;
    } else {
      // datetime-local format (16 chars), convert to ISO
      filters.endDate = parseDateInput(endDate);
    }
  }

  // Parse page
  const page = searchParams.get("page");
  if (page) {
    const parsedPage = Number.parseInt(page, 10);
    if (!Number.isNaN(parsedPage) && parsedPage >= 0) {
      filters.page = parsedPage;
    }
  }

  // Parse size
  const size = searchParams.get("size");
  if (size) {
    const parsedSize = Number.parseInt(size, 10);
    if (!Number.isNaN(parsedSize) && parsedSize > 0) {
      filters.size = parsedSize;
    }
  }

  // Parse sortBy
  const sortBy = searchParams.get("sortBy");
  if (sortBy) {
    filters.sortBy = sortBy;
  }

  // Parse sortDirection
  const sortDirection = searchParams.get("sortDirection");
  if (sortDirection === "ASC" || sortDirection === "DESC") {
    filters.sortDirection = sortDirection;
  }

  return filters;
}

/**
 * Convert AuditLogFilters to URL search params
 * Only includes non-default values to keep URLs clean
 * @param filters - AuditLogFilters object
 * @returns URLSearchParams object
 */
export function filtersToUrlParams(filters: AuditLogFilters): URLSearchParams {
  const params = new URLSearchParams();

  // Only add non-default values
  if (filters.userId !== undefined) {
    params.set("userId", String(filters.userId));
  }

  if (filters.userSearch) {
    params.set("userSearch", filters.userSearch);
  }

  if (filters.action) {
    params.set("action", filters.action);
  }

  if (filters.targetEntity) {
    params.set("targetEntity", filters.targetEntity);
  }

  // Convert ISO date strings to datetime-local format for URL (preserves local time)
  if (filters.startDate) {
    // Convert ISO to datetime-local format for cleaner URLs and to avoid timezone issues
    const dateLocal = formatDateForInput(filters.startDate);
    if (dateLocal) {
      params.set("startDate", dateLocal);
    }
  }

  if (filters.endDate) {
    // Convert ISO to datetime-local format for cleaner URLs and to avoid timezone issues
    const dateLocal = formatDateForInput(filters.endDate);
    if (dateLocal) {
      params.set("endDate", dateLocal);
    }
  }

  // Always include page and size for clarity, but only if not default
  if (filters.page !== undefined && filters.page !== DEFAULT_AUDIT_LOG_FILTERS.page) {
    params.set("page", String(filters.page));
  }

  if (filters.size !== undefined && filters.size !== DEFAULT_AUDIT_LOG_FILTERS.size) {
    params.set("size", String(filters.size));
  }

  if (filters.sortBy && filters.sortBy !== DEFAULT_AUDIT_LOG_FILTERS.sortBy) {
    params.set("sortBy", filters.sortBy);
  }

  if (
    filters.sortDirection &&
    filters.sortDirection !== DEFAULT_AUDIT_LOG_FILTERS.sortDirection
  ) {
    params.set("sortDirection", filters.sortDirection);
  }

  return params;
}

