import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { apiClient } from "../api-client";

// Mock i18n
vi.mock("@/lib/i18n", () => ({
    default: {
        t: vi.fn((key: string) => key),
    },
}));

// Mock config
vi.mock("@/config", () => ({
    API_BASE_URL: "http://test-api.com",
}));

// Helper to create valid fake token
const createValidToken = () => {
    const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }));
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })); // 1 hour valid
    return `${header}.${payload}.signature`;
};

describe("ApiClient", () => {
    const mockFetch = vi.fn();
    globalThis.fetch = mockFetch;

    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        mockFetch.mockReset();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("adds auth token to headers if present", async () => {
        const token = createValidToken();
        localStorage.setItem("auth_token", token);
        mockFetch.mockResolvedValue({
            ok: true,
            headers: new Headers({ "content-type": "application/json" }),
            json: async () => ({}),
        });

        await apiClient.get("/test");

        expect(mockFetch).toHaveBeenCalledWith(
            expect.stringContaining("/test"),
            expect.objectContaining({
                headers: expect.objectContaining({
                    Authorization: `Bearer ${token}`,
                }),
            })
        );
    });

    it("does not add auth token for public endpoints", async () => {
        const token = createValidToken();
        localStorage.setItem("auth_token", token);
        mockFetch.mockResolvedValue({
            ok: true,
            headers: new Headers({ "content-type": "application/json" }),
            json: async () => ({}),
        });

        await apiClient.get("/public/teacher-form-submission/123");

        expect(mockFetch).toHaveBeenCalledWith(
            expect.stringContaining("/public/teacher-form-submission"),
            expect.anything()
        );

        // Check checking headers in 2nd arg
        const callArgs = mockFetch.mock.calls[0];
        const headers = callArgs[1].headers;
        expect(headers).not.toHaveProperty("Authorization");
    });

    it("throws error on 401 unauthorized", async () => {
        const token = createValidToken();
        localStorage.setItem("auth_token", token);
        mockFetch.mockResolvedValue({
            ok: false,
            status: 401,
            statusText: "Unauthorized",
            json: async () => ({ message: "Unauthorized" }),
        });

        const unauthorizedSpy = vi.fn();
        apiClient.setUnauthorizedHandler(unauthorizedSpy);

        await expect(apiClient.get("/protected")).rejects.toThrow();
        // Should NOT call unauthorized handler for regular 401 response (only for session expiration)
        // Actually, logic says:
        /*
          if (
            !isAuthEndpoint &&
            !isPublicEndpoint &&
            errorMessage === "An error occurred"
          ) {
             // call unauthorized
          }
        */
        // Here our mocked response returns { message: "Unauthorized" }, so errorMessage will be "Unauthorized".
        // "Unauthorized" != "An error occurred".
        // So logic dictates it treats it as a backend error, NOT session expired.

        // Let's verify what the code does. If message is "Unauthorized", logic skips session expiration clean up.
        // So onUnauthorized should NOT be called.
        expect(unauthorizedSpy).not.toHaveBeenCalled();
        expect(localStorage.getItem("auth_token")).toBe(token);
    });

    it("handles 401 as session expired when message is generic", async () => {
        const token = createValidToken();
        localStorage.setItem("auth_token", token);
        mockFetch.mockResolvedValue({
            ok: false,
            status: 401,
            statusText: "Unauthorized",
            json: async () => ({}), // Empty response body -> errorMessage defaults to "An error occurred" via code logic? 
            // Logic: errorMessage = "An error occurred" initially.
            // exractErrorMessage({}) returns null.
            // StatusText use? "If response is not JSON, use status text".
            // But here response IS JSON "{}".
            // extract returns null. errorMessage remains "An error occurred".
        });

        const unauthorizedSpy = vi.fn();
        apiClient.setUnauthorizedHandler(unauthorizedSpy);

        await expect(apiClient.get("/protected")).rejects.toThrow("common:errors.sessionExpired");
        expect(unauthorizedSpy).toHaveBeenCalled();
        expect(localStorage.getItem("auth_token")).toBeNull();
    });

    it("handles successful GET request", async () => {
        const mockData = { id: 1, name: "Test" };
        mockFetch.mockResolvedValue({
            ok: true,
            headers: new Headers({ "content-type": "application/json" }),
            json: async () => mockData,
        });

        const result = await apiClient.get("/data");
        expect(result).toEqual(mockData);
    });

    it("handles successful POST request", async () => {
        const mockData = { id: 1 };
        mockFetch.mockResolvedValue({
            ok: true,
            headers: new Headers({ "content-type": "application/json" }),
            json: async () => mockData,
        });

        const payload = { name: "New" };
        const result = await apiClient.post("/data", payload);

        expect(mockFetch).toHaveBeenCalledWith(
            expect.stringContaining("/data"),
            expect.objectContaining({
                method: "POST",
                body: JSON.stringify(payload),
            })
        );
        expect(result).toEqual(mockData);
    });

    it("handles network error", async () => {
        mockFetch.mockRejectedValue(new TypeError("Failed to fetch"));
        await expect(apiClient.get("/data")).rejects.toThrow("common:errors.networkError");
    });

    it("handles timeout error", async () => {
        const timeoutError = new Error("The operation was aborted");
        timeoutError.name = "TimeoutError";
        mockFetch.mockRejectedValue(timeoutError);

        await expect(apiClient.get("/data")).rejects.toThrow("common:errors.requestTimeout");
    });
});
