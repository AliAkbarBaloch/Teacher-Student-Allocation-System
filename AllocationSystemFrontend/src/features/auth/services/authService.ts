import { apiClient } from "@/lib/api-client";
import type {
  LoginCredentials,
  AuthResponse,
  LoginResponse,
  UserProfile,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ChangePasswordRequest,
  UpdateProfileRequest,
} from "../types/auth.types";

/**
 * Authentication service for handling login and authentication operations
 */
export class AuthService {
  /**
   * Login with email and password
   * @param credentials - User login credentials
   * @returns Promise with auth response or throws error
   */
  static async login(credentials: LoginCredentials): Promise<AuthResponse> {
    if (!credentials.email || !credentials.password) {
      throw new Error("Email and password are required");
    }

    try {
      const response: LoginResponse = await apiClient.post<LoginResponse>(
        "/auth/login",
        {
          email: credentials.email,
          password: credentials.password,
        }
      );

      // Transform backend response to frontend format
      return {
        user: {
          id: String(response.userId),
          email: response.email,
          name: response.fullName.split(" ")[0] || response.email.split("@")[0],
          fullName: response.fullName,
          role: response.role,
        },
        token: response.token,
      };
    } catch (error) {
      // Handle specific error messages from backend
      if (error instanceof Error) {
        // Check for account locked error
        if (error.message.toLowerCase().includes("locked")) {
          throw new Error("Account is locked. Please contact administrator or reset your password.");
        }
        // Check for invalid credentials
        if (error.message.toLowerCase().includes("invalid") || 
            error.message.toLowerCase().includes("bad credentials")) {
          throw new Error("Invalid email or password");
        }
        throw error;
      }
      throw new Error("An error occurred during login");
    }
  }

  /**
   * Logout current user
   */
  static async logout(): Promise<void> {
    try {
      await apiClient.post("/auth/logout");
    } catch (error: unknown) {
      console.error("Logout error:", error);
      // Even if logout fails on server, we should still clear local storage
      // Silently fail - logout should always succeed from user perspective
    }
  }

  /**
   * Request password reset
   */
  static async forgotPassword(request: ForgotPasswordRequest): Promise<void> {
    if (!request.email) {
      throw new Error("Email is required");
    }

    await apiClient.post("/auth/forgot-password", {
      email: request.email,
    });
  }

  /**
   * Reset password with token
   */
  static async resetPassword(request: ResetPasswordRequest): Promise<void> {
    if (!request.token || !request.newPassword) {
      throw new Error("Token and new password are required");
    }

    await apiClient.post("/auth/reset-password", {
      token: request.token,
      newPassword: request.newPassword,
    });
  }

  /**
   * Change password for authenticated user
   */
  static async changePassword(request: ChangePasswordRequest): Promise<void> {
    if (!request.currentPassword || !request.newPassword) {
      throw new Error("Current password and new password are required");
    }

    await apiClient.post("/auth/change-password", {
      currentPassword: request.currentPassword,
      newPassword: request.newPassword,
    });
  }

  /**
   * Get current user profile
   */
  static async getProfile(): Promise<UserProfile> {
    return apiClient.get<UserProfile>("/auth/profile");
  }

  /**
   * Update user profile
   */
  static async updateProfile(request: UpdateProfileRequest): Promise<UserProfile> {
    return apiClient.put<UserProfile>("/auth/profile", request);
  }

  /**
   * Get current user from stored token
   */
  static getCurrentUser(): AuthResponse["user"] | null {
    const token = localStorage.getItem("auth_token");
    const userStr = localStorage.getItem("auth_user");
    
    if (token && userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    
    return null;
  }
}

