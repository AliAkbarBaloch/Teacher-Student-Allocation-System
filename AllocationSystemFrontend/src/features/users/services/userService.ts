//API helper. fetch is inside  
import { apiClient } from "@/lib/api-client";
import type {
  User,
  CreateUserRequest,
  UpdateUserRequest,
  UsersListParams,
  SpringPage,
  PasswordResetRequest,
} from "@/features/users/types/user.types";

import { buildQueryParams } from "@/lib/utils/query-params";

//Backend response wrapper type 
//{"success" : true,
//  "message": "something",
//  "data": ACTUAL_RESULT
// }
//T - type of data, depends on endpoint 
// ApiResponse<User> means {data: User}
// Apiresponse<SpringPage<User>> means {data: page}
type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

//the shape of stats returned by /users/statistics
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  suspendedUsers: number;
  lockedUsers: number;
  adminUsers: number;
  regularUsers: number;
}

/**
 * User service for /users endpoints.
 * - apiClient.get<T>() returns T directly (not { data: T })
 * - backend wraps responses as ApiResponse<T>
 * - so we call apiClient.get<ApiResponse<T>>() and then return response.data
 */
export class UserService {
  // ---------------------------
  // Statistics
  // GET /users/statistics
  // ---------------------------
  static async getStatistics(): Promise<UserStatistics> {
    //call endpoint /users/statistics 
    const response = await apiClient.get<ApiResponse<UserStatistics>>("/users/statistics");
    return response.data;
  }

  // List users (pagination + filters)
  // GET /users
  static async getPaginated(params: UsersListParams = {}): Promise<SpringPage<User>> {
    const query = buildQueryParams(params);

    //build URL
    //if query has something - attach it to URL, overwise just /users 
    const url = query.toString() ? `/users?${query.toString()}` : "/users";

    //request 
    //backend returns wrapper with a page inside data 
    const response = await apiClient.get<ApiResponse<SpringPage<User>>>(url);

    //return the page inside the wrapper 
    return response.data;
  }

  // ---------------------------
  // Get user by ID
  // GET /users/{id}
  // ---------------------------
  static async getById(id: number): Promise<User> {

    const response = await apiClient.get<ApiResponse<User>>(`/users/${id}`);

    return response.data;
  }

  // ---------------------------
  // Create user
  // POST /users
  // ---------------------------
  static async create(payload: CreateUserRequest): Promise<User> {

    const response = await apiClient.post<ApiResponse<User>>("/users", payload);

    return response.data;

  }

  // ---------------------------
  // Update user
  // PUT /users/{id}
  // ---------------------------
  static async update(id: number, payload: UpdateUserRequest): Promise<User> {
    const response = await apiClient.put<ApiResponse<User>>(`/users/${id}`, payload);
    return response.data;
  }

  // ---------------------------
  // Delete user
  // DELETE /users/{id}
  // ---------------------------
  static async delete(id: number): Promise<void> {
    // backend returns 204, our client returns {} for non-json responses, which is fine
    await apiClient.delete<void>(`/users/${id}`);
  }

  // ---------------------------
  // Activate / Deactivate
  // PATCH /users/{id}/activate
  // PATCH /users/{id}/deactivate
  // ---------------------------
  static async activate(id: number): Promise<User> {
    const response = await apiClient.patch<ApiResponse<User>>(`/users/${id}/activate`);
    return response.data;
  }

  static async deactivate(id: number): Promise<User> {
    const response = await apiClient.patch<ApiResponse<User>>(`/users/${id}/deactivate`);
    return response.data;
  }

  // ---------------------------
  // Reset password
  // POST /users/{id}/reset-password
  // backend expects: { newPassword }
  // ---------------------------
  static async resetPassword(id: number, payload: PasswordResetRequest): Promise<User> {
    const response = await apiClient.post<ApiResponse<User>>(`/users/${id}/reset-password`, {
      newPassword: payload.newPassword,
    });
    return response.data;
  }
}
