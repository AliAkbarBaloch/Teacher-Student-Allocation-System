import type { LoginCredentials, AuthResponse } from "../types/auth.types";

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
    // Simulate API call delay
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Mock validation
    if (!credentials.email || !credentials.password) {
      throw new Error("Email and password are required");
    }

    // Mock email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(credentials.email)) {
      throw new Error("Please enter a valid email address");
    }

    // Mock password validation
    if (credentials.password.length < 6) {
      throw new Error("Password must be at least 6 characters long");
    }

    // Mock successful login
    // In a real app, this would be an API call
    return {
      user: {
        id: "1",
        email: credentials.email,
        name: credentials.email.split("@")[0],
      },
      token: "mock-jwt-token-" + Date.now(),
    };
  }

  /**
   * Logout current user
   */
  static async logout(): Promise<void> {
    // In a real app, this would invalidate the token on the server
    await new Promise((resolve) => setTimeout(resolve, 300));
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

