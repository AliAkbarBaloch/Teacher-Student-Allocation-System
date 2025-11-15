import { useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { AuthService } from "../services/authService";
import type { LoginCredentials, AuthError, AuthState } from "../types/auth.types";
import { ROUTES } from "@/config/routes";

const initialState: AuthState = {
  user: AuthService.getCurrentUser(),
  token: localStorage.getItem("auth_token"),
  isAuthenticated: !!localStorage.getItem("auth_token"),
  isLoading: false,
  error: null,
};

export function useAuth() {
  const [authState, setAuthState] = useState<AuthState>(initialState);
  const navigate = useNavigate();

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
      
      // Clear auth data
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
    } catch (error) {
      setAuthState((prev) => ({
        ...prev,
        isLoading: false,
        error: {
          message: error instanceof Error ? error.message : "Logout failed",
          field: "general",
        },
      }));
    }
  }, [navigate]);

  const clearError = useCallback(() => {
    setAuthState((prev) => ({ ...prev, error: null }));
  }, []);

  const updateProfile = useCallback((updates: { name?: string; email?: string }) => {
    if (!authState.user) return;

    const updatedUser = {
      ...authState.user,
      ...updates,
    };

    // Update localStorage
    localStorage.setItem("auth_user", JSON.stringify(updatedUser));

    // Update state
    setAuthState((prev) => ({
      ...prev,
      user: updatedUser,
    }));
  }, [authState.user]);

  return {
    ...authState,
    login,
    logout,
    clearError,
    updateProfile,
  };
}

