import type { AuditAction } from "../types/auditLog.types";
import { AuditAction as AuditActionConst } from "../types/auditLog.types";

type BadgeVariant = "default" | "secondary" | "destructive" | "outline";

/**
 * Action groups for badge styling
 */
const CREATE_ACTIONS = new Set<AuditAction>([
  AuditActionConst.CREATE,
  AuditActionConst.PASSWORD_RESET,
]);

const UPDATE_ACTIONS = new Set<AuditAction>([
  AuditActionConst.UPDATE,
  AuditActionConst.PROFILE_UPDATED,
  AuditActionConst.PERMISSION_CHANGE,
]);

const DELETE_ACTIONS = new Set<AuditAction>([
  AuditActionConst.DELETE,
  AuditActionConst.ACCOUNT_LOCKED,
]);

/**
 * Gets the badge variant for an audit action
 * @param action - Audit action
 * @returns Badge variant
 */
export function getActionBadgeVariant(action: AuditAction): BadgeVariant {
  if (CREATE_ACTIONS.has(action)) {
    return "default";
  }
  if (UPDATE_ACTIONS.has(action)) {
    return "secondary";
  }
  if (DELETE_ACTIONS.has(action)) {
    return "destructive";
  }
  // LOGIN, LOGOUT, and other actions use outline variant (neutral styling)
  return "outline";
}

