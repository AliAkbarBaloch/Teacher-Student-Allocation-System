/**
 * Audit log types
 */

export const AuditAction = {
  CREATE: "CREATE",
  UPDATE: "UPDATE",
  DELETE: "DELETE",
  VIEW: "VIEW",
  LOGIN: "LOGIN",
  LOGOUT: "LOGOUT",
  LOGIN_FAILED: "LOGIN_FAILED",
  PASSWORD_CHANGE: "PASSWORD_CHANGE",
  PASSWORD_RESET_REQUESTED: "PASSWORD_RESET_REQUESTED",
  PASSWORD_RESET: "PASSWORD_RESET",
  PASSWORD_CHANGE_FAILED: "PASSWORD_CHANGE_FAILED",
  ACCOUNT_LOCKED: "ACCOUNT_LOCKED",
  PROFILE_UPDATED: "PROFILE_UPDATED",
  PERMISSION_CHANGE: "PERMISSION_CHANGE",
  ROLE_ASSIGNMENT: "ROLE_ASSIGNMENT",
  PLAN_CREATED: "PLAN_CREATED",
  PLAN_MODIFIED: "PLAN_MODIFIED",
  PLAN_DELETED: "PLAN_DELETED",
  ALLOCATION_ASSIGNED: "ALLOCATION_ASSIGNED",
  ALLOCATION_MODIFIED: "ALLOCATION_MODIFIED",
  EXPORT: "EXPORT",
  IMPORT: "IMPORT",
  SYSTEM_CONFIG_CHANGE: "SYSTEM_CONFIG_CHANGE",
} as const;

export type AuditAction = typeof AuditAction[keyof typeof AuditAction];

export interface AuditLog {
  id: number;
  userId: number | null;
  userIdentifier: string;
  eventTimestamp: string;
  action: AuditAction;
  targetEntity: string;
  targetRecordId: string | null;
  previousValue: string | null;
  newValue: string | null;
  description: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface AuditLogFilters {
  userId?: number;
  userSearch?: string; // For searching by ID or email
  action?: AuditAction;
  targetEntity?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: "ASC" | "DESC";
}

export interface PaginatedAuditLogResponse {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface AuditLogStats {
  actionStatistics: Record<string, number>;
  entityStatistics: Record<string, number>;
  userActivityStatistics: Record<string, number>;
  totalLogs: number;
}

