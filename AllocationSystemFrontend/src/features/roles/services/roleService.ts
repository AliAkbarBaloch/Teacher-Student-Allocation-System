import { apiClient } from "@/lib/api-client";
import type {
  Role,
  CreateRoleRequest,
  UpdateRoleRequest,
  RoleResponse,
  RolesListResponse,
  PaginatedRolesResponse,
} from "../types/role.types";

import { buildQueryParams } from "@/lib/utils/query-params";

/**
 * Role service for handling role management operations
 */
export class RoleService {
  /**
   * Get all roles
   */
  static async getAll(): Promise<Role[]> {
    const response = await apiClient.get<RolesListResponse>("/roles");
    return response.data;
  }

  /**
   * Get paginated roles
   */
  static async getPaginated(params: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortOrder?: "asc" | "desc";
    searchValue?: string;
  }): Promise<PaginatedRolesResponse["data"]> {
    const queryParams = buildQueryParams(params);

    const response = await apiClient.get<PaginatedRolesResponse>(
      `/roles/paginate?${queryParams.toString()}`
    );
    return response.data;
  }

  /**
   * Get role by ID
   */
  static async getById(id: number): Promise<Role> {
    const response = await apiClient.get<RoleResponse>(`/roles/${id}`);
    return response.data;
  }

  /**
   * Create a new role
   */
  static async create(role: CreateRoleRequest): Promise<Role> {
    const response = await apiClient.post<RoleResponse>("/roles", role);
    return response.data;
  }

  /**
   * Update an existing role
   */
  static async update(id: number, role: UpdateRoleRequest): Promise<Role> {
    const response = await apiClient.put<RoleResponse>(`/roles/${id}`, role);
    return response.data;
  }

  /**
   * Delete a role
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/roles/${id}`);
  }
}

