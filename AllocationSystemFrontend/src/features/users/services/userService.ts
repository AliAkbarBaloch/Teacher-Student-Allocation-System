import { apiClient } from "@/lib/api-client";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

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
 * User service for handling user-related operations
 */
export class UserService {
  /**
   * Get user statistics
   */
  static async getStatistics(): Promise<UserStatistics> {
    const response = await apiClient.get<ApiResponse<UserStatistics>>("/users/statistics");
    return response.data;
  }
}
