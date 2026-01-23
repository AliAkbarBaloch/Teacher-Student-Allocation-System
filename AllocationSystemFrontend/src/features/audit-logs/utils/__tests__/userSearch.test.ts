import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { resolveUserSearch } from "../userSearch";
import { userSearchCache } from "../userSearchCache";
import { apiClient } from "@/lib/api-client";

// Mock the apiClient
vi.mock("@/lib/api-client", () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

describe("resolveUserSearch", () => {
  beforeEach(() => {
    // Clear cache before each test
    userSearchCache.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    userSearchCache.clear();
  });

  it("should return null for empty string", async () => {
    const result = await resolveUserSearch("");
    expect(result).toBeNull();
  });

  it("should return null for whitespace-only string", async () => {
    const result = await resolveUserSearch("   ");
    expect(result).toBeNull();
  });

  it("should return user ID directly for numeric string", async () => {
    const result = await resolveUserSearch("123");
    expect(result).toBe(123);
    expect(apiClient.get).not.toHaveBeenCalled();
  });

  it("should cache numeric user ID", async () => {
    const result1 = await resolveUserSearch("456");
    const result2 = await resolveUserSearch("456");

    expect(result1).toBe(456);
    expect(result2).toBe(456);
    expect(apiClient.get).not.toHaveBeenCalled();
  });

  it("should return null for zero or negative numbers", async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ content: [] });
    const result1 = await resolveUserSearch("0");
    expect(result1).toBeNull();

    const result2 = await resolveUserSearch("-1");
    expect(result2).toBeNull();
  });

  it("should lookup user by email and cache result", async () => {
    const mockUserResponse = {
      content: [
        { id: 789, email: "test@example.com" },
      ],
    };

    vi.mocked(apiClient.get).mockResolvedValue(mockUserResponse);

    const result1 = await resolveUserSearch("test@example.com");
    const result2 = await resolveUserSearch("test@example.com");

    expect(result1).toBe(789);
    expect(result2).toBe(789);
    expect(apiClient.get).toHaveBeenCalledTimes(1); // Should only call once due to caching
  });

  it("should handle case-insensitive email lookup", async () => {
    const mockUserResponse = {
      content: [
        { id: 789, email: "Test@Example.com" },
      ],
    };

    vi.mocked(apiClient.get).mockResolvedValue(mockUserResponse);

    const result = await resolveUserSearch("test@example.com");
    expect(result).toBe(789);
  });

  it("should return null if user not found by email", async () => {
    const mockUserResponse = {
      content: [],
    };

    vi.mocked(apiClient.get).mockResolvedValue(mockUserResponse);

    const result = await resolveUserSearch("notfound@example.com");
    expect(result).toBeNull();
  });

  it("should return null if email doesn't match exactly", async () => {
    const mockUserResponse = {
      content: [
        { id: 789, email: "other@example.com" },
      ],
    };

    vi.mocked(apiClient.get).mockResolvedValue(mockUserResponse);

    const result = await resolveUserSearch("test@example.com");
    expect(result).toBeNull();
  });

  it("should handle API errors gracefully", async () => {
    const consoleSpy = vi.spyOn(console, "warn").mockImplementation(() => { });
    vi.mocked(apiClient.get).mockRejectedValue(new Error("API Error"));

    const result = await resolveUserSearch("test@example.com");

    expect(result).toBeNull();
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });

  it("should trim whitespace from input", async () => {
    const result = await resolveUserSearch("  123  ");
    expect(result).toBe(123);
  });
});

