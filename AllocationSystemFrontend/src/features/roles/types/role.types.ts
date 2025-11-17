/**
 * Role management types
 */

export interface Role {
  id: number;
  title: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRoleRequest {
  title: string;
  description: string;
}

export interface UpdateRoleRequest {
  title: string;
  description: string;
}

export interface RoleResponse {
  success: boolean;
  message: string;
  data: Role;
}

export interface RolesListResponse {
  success: boolean;
  message: string;
  data: Role[];
}

export interface PaginatedRolesResponse {
  success: boolean;
  message: string;
  data: {
    content: Role[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
  };
}

/**
 * Reserved role titles that must remain immutable in the UI.
 * These roles still originate from the backend/database; this list simply
 * captures which titles should be considered system-protected.
 */
export const SYSTEM_PROTECTED_ROLES = ["ADMIN"];

export function isSystemProtectedRole(roleTitle: string): boolean {
  const normalized = roleTitle.trim().toLowerCase();
  return SYSTEM_PROTECTED_ROLES.some(
    (protectedTitle) => protectedTitle.toLowerCase() === normalized
  );
}

/**
 * Determine if a role title represents a system-protected role.
 * Backend controls the actual role names; frontend only infers protection
 * from the role titles it receives (e.g., any role containing "admin").
 */
const PROTECTED_ROLE_KEYWORDS = ["admin"];

export function isBackendProtectedRole(title: string): boolean {
  const normalized = title.trim().toLowerCase();
  return PROTECTED_ROLE_KEYWORDS.some((keyword) => normalized.includes(keyword));
}

