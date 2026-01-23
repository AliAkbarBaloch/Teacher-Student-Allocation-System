import { renderHook, act, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { useAuth } from "../useAuth";
import { AuthService } from "../../services/authService";
import { apiClient } from "@/lib/api-client";
import { ROUTES } from "@/config/routes";

// Mock dependencies
const mockNavigate = vi.fn();
vi.mock("react-router-dom", () => ({
    useNavigate: () => mockNavigate,
}));

vi.mock("../../services/authService", () => ({
    AuthService: {
        getCurrentUser: vi.fn(),
        login: vi.fn(),
        logout: vi.fn(),
        getProfile: vi.fn(),
    },
}));

vi.mock("@/lib/api-client", () => ({
    apiClient: {
        setUnauthorizedHandler: vi.fn(),
    },
}));

describe("useAuth", () => {
    const mockUser = {
        id: "1",
        email: "test@example.com",
        name: "Test",
        fullName: "Test User",
        role: "ADMIN",
    };

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        mockNavigate.mockClear();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("initializes with default state when no token exists", () => {
        vi.mocked(AuthService.getCurrentUser).mockReturnValue(null);

        const { result } = renderHook(() => useAuth());

        expect(result.current.user).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
        expect(result.current.isLoading).toBe(false);
    });

    it("initializes with user data when token exists in localStorage", () => {
        localStorage.setItem("auth_token", "fake-token");

        vi.mocked(AuthService.getCurrentUser).mockReturnValue(mockUser);

        const { result } = renderHook(() => useAuth());

        expect(result.current.user).toEqual(mockUser);
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.token).toBe("fake-token");
    });

    it("sets unauthorized handler on mount", () => {
        renderHook(() => useAuth());
        expect(apiClient.setUnauthorizedHandler).toHaveBeenCalled();
    });

    it("handles successful login", async () => {
        const loginResponse = {
            token: "new-token",
            user: mockUser,
        };

        vi.mocked(AuthService.login).mockResolvedValue(loginResponse);

        const { result } = renderHook(() => useAuth());

        await act(async () => {
            await result.current.login({ email: "test@example.com", password: "password", rememberMe: true });
        });

        expect(localStorage.getItem("auth_token")).toBe("new-token");
        expect(localStorage.getItem("remember_me")).toBe("true");
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.user).toEqual(mockUser);
        expect(mockNavigate).toHaveBeenCalledWith(ROUTES.main.home);
    });

    it("handles failed login", async () => {
        const error = new Error("Invalid password");
        vi.mocked(AuthService.login).mockRejectedValue(error);

        const { result } = renderHook(() => useAuth());

        await act(async () => {
            try {
                await result.current.login({ email: "test@example.com", password: "wrong" });
            } catch {
                // expected error
            }
        });

        await waitFor(() => {
            expect(result.current.error).toEqual({
                message: "Invalid password",
                field: "password",
            });
        });
    });

    it("handles logout", async () => {
        localStorage.setItem("auth_token", "token");
        vi.mocked(AuthService.logout).mockResolvedValue(undefined);

        const { result } = renderHook(() => useAuth());

        await act(async () => {
            await result.current.logout();
        });

        expect(localStorage.getItem("auth_token")).toBeNull();
        expect(result.current.user).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
        expect(mockNavigate).toHaveBeenCalledWith("/login");
    });

    it("updates profile", () => {
        localStorage.setItem("auth_token", "token");

        vi.mocked(AuthService.getCurrentUser).mockReturnValue(mockUser);

        const { result } = renderHook(() => useAuth());

        act(() => {
            result.current.updateProfile({ fullName: "Updated Name" });
        });

        expect(result.current.user?.fullName).toBe("Updated Name");
        expect(JSON.parse(localStorage.getItem("auth_user") || "{}")).toMatchObject({
            fullName: "Updated Name",
        });
    });

    it("refreshes user data", async () => {
        localStorage.setItem("auth_token", "token");
        const updatedProfile = {
            id: 1,
            email: "test@example.com",
            fullName: "Refreshed Name",
            name: "Test",
            role: "ADMIN",
            enabled: true,
            accountLocked: false,
            accountStatus: "active",
            failedLoginAttempts: 0
        };

        vi.mocked(AuthService.getProfile).mockResolvedValue(updatedProfile);

        const { result } = renderHook(() => useAuth());

        await act(async () => {
            await result.current.refreshUser();
        });

        expect(result.current.user?.fullName).toBe("Refreshed Name");
    });

    it("clears auth on refresh failure", async () => {
        localStorage.setItem("auth_token", "token");
        vi.mocked(AuthService.getProfile).mockRejectedValue(new Error("Refresh failed"));

        const { result } = renderHook(() => useAuth());

        await act(async () => {
            await result.current.refreshUser();
        });

        expect(localStorage.getItem("auth_token")).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
    });

    it("syncs state on storage event", () => {
        const { result } = renderHook(() => useAuth());

        // Simulate initial state
        expect(result.current.isAuthenticated).toBe(false);

        // Simulate storage event from another tab
        act(() => {
            localStorage.setItem("auth_token", "new-token");

            vi.mocked(AuthService.getCurrentUser).mockReturnValue(mockUser);

            window.dispatchEvent(new StorageEvent("storage", {
                key: "auth_token",
                newValue: "new-token",
                storageArea: localStorage
            }));
        });

        // The hook calls getInitialAuthState which calls AuthService.getCurrentUser
        // We mocked it above so it should pick it up
        expect(result.current.isAuthenticated).toBe(true);
    });
});
