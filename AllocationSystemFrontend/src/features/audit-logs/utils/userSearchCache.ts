/**
 * Simple in-memory cache for user search results
 * Key: search string (email or ID)
 * Value: user ID
 */
class UserSearchCache {
  private cache = new Map<string, number>();
  private readonly maxSize = 100;
  private readonly ttl = 5 * 60 * 1000; // 5 minutes
  private timestamps = new Map<string, number>();

  /**
   * Get cached user ID for a search string
   */
  get(search: string): number | null {
    const key = search.toLowerCase().trim();
    const timestamp = this.timestamps.get(key);

    // Check if cache entry exists and is still valid
    if (timestamp && Date.now() - timestamp < this.ttl) {
      return this.cache.get(key) ?? null;
    }

    // Remove expired entry
    if (timestamp) {
      this.cache.delete(key);
      this.timestamps.delete(key);
    }

    return null;
  }

  /**
   * Set cached user ID for a search string
   */
  set(search: string, userId: number): void {
    const key = search.toLowerCase().trim();

    // Evict oldest entries if cache is full
    if (this.cache.size >= this.maxSize && !this.cache.has(key)) {
      const oldestKey = this.timestamps.entries().next().value?.[0];
      if (oldestKey) {
        this.cache.delete(oldestKey);
        this.timestamps.delete(oldestKey);
      }
    }

    this.cache.set(key, userId);
    this.timestamps.set(key, Date.now());
  }

  /**
   * Clear the cache
   */
  clear(): void {
    this.cache.clear();
    this.timestamps.clear();
  }

  /**
   * Remove expired entries
   */
  cleanup(): void {
    const now = Date.now();
    for (const [key, timestamp] of this.timestamps.entries()) {
      if (now - timestamp >= this.ttl) {
        this.cache.delete(key);
        this.timestamps.delete(key);
      }
    }
  }
}

// Singleton instance
export const userSearchCache = new UserSearchCache();

// Cleanup expired entries every minute
if (typeof window !== "undefined") {
  setInterval(() => {
    userSearchCache.cleanup();
  }, 60 * 1000);
}

