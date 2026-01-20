import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";

import { ProtectedRoute } from "@/components/common/ProtectedRoute";
import { ROUTES } from "@/config/routes";
import type { AuthState } from "@/features/auth/types/auth.types";

vi.mock("@/features/auth/hooks/useAuth", () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from "@/features/auth/hooks/useAuth";

type UseAuthReturn = AuthState & {
  login: (...args: unknown[]) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
  updateProfile: (updates: { name?: string; email?: string; fullName?: string }) => void;
  refreshUser: () => Promise<void>;
};

const mockUseAuth = vi.mocked(useAuth);

function renderWithRoutes(ui: React.ReactNode, initialEntry: string) {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path={ROUTES.auth.login} element={<div>Login Page</div>} />
        <Route path={ROUTES.main.home} element={<div>Home Page</div>} />
        <Route path="/protected" element={ui} />
      </Routes>
    </MemoryRouter>
  );
}

const baseAuthState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
};

const baseAuthFunctions: Pick<
  UseAuthReturn,
  "login" | "logout" | "clearError" | "updateProfile" | "refreshUser"
> = {
  login: vi.fn(async () => {}),
  logout: vi.fn(async () => {}),
  clearError: vi.fn(() => {}),
  updateProfile: vi.fn(() => {}),
  refreshUser: vi.fn(async () => {}),
};

describe("ProtectedRoute", () => {
  it("shows loading state while auth is loading", () => {
    const value: UseAuthReturn = {
      ...baseAuthState,
      isLoading: true,
      ...baseAuthFunctions,
    };
    mockUseAuth.mockReturnValue(value);

    renderWithRoutes(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>,
      "/protected"
    );

    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("redirects to login when unauthenticated", () => {
    const value: UseAuthReturn = {
      ...baseAuthState,
      isAuthenticated: false,
      user: null,
      ...baseAuthFunctions,
    };
    mockUseAuth.mockReturnValue(value);

    renderWithRoutes(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>,
      "/protected"
    );

    expect(screen.getByText("Login Page")).toBeInTheDocument();
    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
  });

  it("renders children when authenticated", () => {
    const value: UseAuthReturn = {
      ...baseAuthState,
      isAuthenticated: true,
      user: { id: "1", email: "a@b.com", name: "A", fullName: "A B", role: "USER" },
      ...baseAuthFunctions,
    };
    mockUseAuth.mockReturnValue(value);

    renderWithRoutes(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>,
      "/protected"
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });

  it("redirects non-admins to home when requireAdmin=true", () => {
    const value: UseAuthReturn = {
      ...baseAuthState,
      isAuthenticated: true,
      user: { id: "1", email: "a@b.com", name: "A", fullName: "A B", role: "USER" },
      ...baseAuthFunctions,
    };
    mockUseAuth.mockReturnValue(value);

    renderWithRoutes(
      <ProtectedRoute requireAdmin>
        <div>Admin Content</div>
      </ProtectedRoute>,
      "/protected"
    );

    expect(screen.getByText("Home Page")).toBeInTheDocument();
    expect(screen.queryByText("Admin Content")).not.toBeInTheDocument();
  });
});

