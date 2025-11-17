import { apiClient } from "@/lib/api-client";
import { userSearchCache } from "./userSearchCache";

/**
 * Resolves a user search string (ID or email) to a user ID
 * Uses caching to reduce API calls
 * @param userSearch - User ID (number as string) or email address
 * @returns Promise resolving to user ID or null if not found
 */
export async function resolveUserSearch(userSearch: string): Promise<number | null> {
  if (!userSearch || !userSearch.trim()) {
    return null;
  }

  const trimmed = userSearch.trim();

  // Check cache first
  const cached = userSearchCache.get(trimmed);
  if (cached !== null) {
    return cached;
  }

  const userId = Number.parseInt(trimmed, 10);

  // If it's a valid number, use it directly and cache it
  if (!Number.isNaN(userId) && userId > 0) {
    userSearchCache.set(trimmed, userId);
    return userId;
  }

  // Otherwise, try to find user by email
  try {
    const userResponse = await apiClient.get<{ content: Array<{ id: number; email: string }> }>(
      `/users?search=${encodeURIComponent(trimmed)}&size=1`
    );
    
    if (userResponse.content && userResponse.content.length > 0) {
      const foundUser = userResponse.content.find(
        (u) => u.email.toLowerCase() === trimmed.toLowerCase()
      );
      if (foundUser) {
        // Cache the result
        userSearchCache.set(trimmed, foundUser.id);
        return foundUser.id;
      }
    }
  } catch (error) {
    // Log error but don't throw - allow graceful degradation
    console.warn("Failed to lookup user by email:", error);
  }

  return null;
}

