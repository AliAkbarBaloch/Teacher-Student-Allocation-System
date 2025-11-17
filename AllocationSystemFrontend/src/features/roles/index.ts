export { RoleService } from "./services/roleService";
export { RoleForm } from "./components/RoleForm";
export type {
  Role,
  CreateRoleRequest,
  UpdateRoleRequest,
  RoleResponse,
  RolesListResponse,
  PaginatedRolesResponse,
} from "./types/role.types";
export { SYSTEM_PROTECTED_ROLES, isSystemProtectedRole } from "./types/role.types";

