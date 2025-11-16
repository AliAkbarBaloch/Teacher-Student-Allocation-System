import { useState, useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { AuthService } from "../services/authService";
import { apiClient } from "@/lib/api-client";
import type { LoginCredentials, AuthError, AuthState } from "../types/auth.types";
import { ROUTES } from "@/config/routes";

/**
 * Get initial auth state from localStorage
 */
function getInitialAuthState(): AuthState {
  const token = localStorage.getItem("auth_token");
  const user = AuthService.getCurrentUser();
  
  return {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading: false,
    error: null,
  };
}

export function useAuth() {
  const [authState, setAuthState] = useState<AuthState>(getInitialAuthState);
  const navigate = useNavigate();

  // Set up unauthorized handler for API client
  useEffect(() => {
    const handleUnauthorized = () => {
      // Clear auth state and redirect to login
      localStorage.removeItem("auth_token");
      localStorage.removeItem("auth_user");
      localStorage.removeItem("remember_me");
      setAuthState({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
      navigate("/login");
    };
    apiClient.setUnauthorizedHandler(handleUnauthorized);
    
    // Cleanup on unmount
    return () => {
      apiClient.setUnauthorizedHandler(() => {});
    };
  }, [navigate]);

  // Sync state with localStorage when storage changes (from other tabs)
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      // Only handle storage events from other tabs/windows
      if (e.key === "auth_token" || e.key === "auth_user") {
        setAuthState(getInitialAuthState());
      }
    };

    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  const login = useCallback(async (credentials: LoginCredentials) => {
    setAuthState((prev) => ({ ...prev, isLoading: true, error: null }));

    try {
      const response = await AuthService.login(credentials);

      // Store auth data
      localStorage.setItem("auth_token", response.token);
      localStorage.setItem("auth_user", JSON.stringify(response.user));
      
      if (credentials.rememberMe) {
        localStorage.setItem("remember_me", "true");
      } else {
        localStorage.removeItem("remember_me");
      }

      setAuthState({
        user: response.user,
        token: response.token,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });

      // Navigate to home page after successful login
      navigate(ROUTES.main.home);
    } catch (error) {
      const authError: AuthError = {
        message: error instanceof Error ? error.message : "An error occurred during login",
        field: "general",
      };

      // Determine field-specific errors
      if (authError.message.toLowerCase().includes("email")) {
        authError.field = "email";
      } else if (authError.message.toLowerCase().includes("password")) {
        authError.field = "password";
      }

      setAuthState((prev) => ({
        ...prev,
        isLoading: false,
        error: authError,
      }));

      throw error;
    }
  }, [navigate]);

  const logout = useCallback(async () => {
    setAuthState((prev) => ({ ...prev, isLoading: true }));

    try {
      await AuthService.logout();
    } catch (error) {
      // Continue with logout even if API call fails
      console.error("Logout API error:", error);
    } finally {
      // Always clear auth data, even if API call fails
      localStorage.removeItem("auth_token");
      localStorage.removeItem("auth_user");
      localStorage.removeItem("remember_me");

      // Force state reset
      setAuthState({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });

      navigate("/login");
    }
  }, [navigate]);

  const clearError = useCallback(() => {
    setAuthState((prev) => ({ ...prev, error: null }));
  }, []);

  const updateProfile = useCallback((updates: { name?: string; email?: string; fullName?: string }) => {
    if (!authState.user) return;

    // Preserve all existing user properties, especially role
    const updatedUser = {
      ...authState.user,
      ...updates,
      // Ensure role is preserved
      role: authState.user.role,
    };

    // Update localStorage
    localStorage.setItem("auth_user", JSON.stringify(updatedUser));

    // Update state
    setAuthState((prev) => ({
      ...prev,
      user: updatedUser,
    }));
  }, [authState.user]);

  /**
   * Refresh user data from backend
   */
  const refreshUser = useCallback(async () => {
    const token = localStorage.getItem("auth_token");
    if (!token) {
      setAuthState({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
      return;
    }

    try {
      const profile = await AuthService.getProfile();
      const user = {
        id: String(profile.id),
        email: profile.email,
        name: profile.fullName.split(" ")[0] || profile.email.split("@")[0],
        fullName: profile.fullName,
        role: profile.role,
      };

      localStorage.setItem("auth_user", JSON.stringify(user));
      setAuthState((prev) => ({
        ...prev,
        user,
        isAuthenticated: true,
      }));
    } catch {
      // If refresh fails, clear auth state
      localStorage.removeItem("auth_token");
      localStorage.removeItem("auth_user");
      setAuthState({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    }
  }, []);

  return {
    ...authState,
    login,
    logout,
    clearError,
    updateProfile,
    refreshUser,
  };
}

