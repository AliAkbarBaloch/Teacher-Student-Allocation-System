import { API_BASE_URL } from "@/config";
import i18n from "@/lib/i18n";

/**
 * API Client for making HTTP requests to the backend
 */
class ApiClient {
  private baseUrl: string;
  private onUnauthorized?: () => void;

  constructor() {
    this.baseUrl = API_BASE_URL || "http://localhost:8080/api";
  }

  /**
   * Set callback for unauthorized (401) responses
   */
  setUnauthorizedHandler(handler: () => void) {
    this.onUnauthorized = handler;
  }

  /**
   * Get the authorization token from localStorage
   */
  private getAuthToken(): string | null {
    return localStorage.getItem("auth_token");
  }

  /**
   * Check if token is expired (basic check - backend is source of truth)
   */
  private isTokenExpired(token: string): boolean {
    try {
      const [, payloadSegment] = token.split(".");
      if (!payloadSegment) {
        return true;
      }

      const payload = JSON.parse(atob(payloadSegment)) as { exp?: number };

      if (typeof payload.exp !== "number") {
        return true;
      }

      const exp = payload.exp * 1000; // Convert to milliseconds
      return Date.now() >= exp;
    } catch {
      // Treat any malformed token as expired to force a refresh
      return true;
    }
  }

  /**
   * Check if endpoint is an auth endpoint that doesn't require token validation
   */
  private isAuthEndpoint(endpoint: string): boolean {
    return (
      endpoint.includes("/auth/login") ||
          endpoint.includes("/auth/forgot-password") || 
      endpoint.includes("/auth/reset-password")
    );
  }

  /**
   * Check if endpoint is a public endpoint that doesn't require authentication
   */
  private isPublicEndpoint(endpoint: string): boolean {
    return endpoint.includes("/public/teacher-form-submission/");
  }

  /**
   * Handle expired token by clearing auth data and calling unauthorized handler
   */
  private handleExpiredToken(): void {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
    if (this.onUnauthorized) {
      this.onUnauthorized();
    }
  }

  /**
   * Validate token expiration before making request
   */
  private validateTokenForRequest(endpoint: string): void {
    // Skip validation for auth endpoints and public endpoints
    if (this.isAuthEndpoint(endpoint) || this.isPublicEndpoint(endpoint)) {
      return;
    }
    
    const token = this.getAuthToken();
    if (token && this.isTokenExpired(token)) {
      this.handleExpiredToken();
      throw new Error(i18n.t("common:errors.sessionExpired"));
    }
  }

  /**
   * Build request headers with authentication token
   */
  private buildHeaders(
    options?: RequestInit,
    extraOptions?: { omitJsonContentType?: boolean; endpoint?: string }
  ): Record<string, string> {
    const token = this.getAuthToken();
    const headers: Record<string, string> =
      options?.headers &&
      typeof options.headers === "object" &&
      !(options.headers instanceof Headers)
        ? { ...(options.headers as Record<string, string>) }
        : {};

    if (!extraOptions?.omitJsonContentType && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    // Only add auth token if endpoint is not public
    if (
      token &&
      (!extraOptions?.endpoint ||
        !this.isPublicEndpoint(extraOptions.endpoint))
    ) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  /**
   * Handle network errors and transform them to user-friendly messages
   */
  private handleNetworkError(error: unknown): never {
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error(i18n.t("common:errors.networkError"));
    }
    if (
      error instanceof Error &&
      (error.name === "AbortError" || error.name === "TimeoutError")
    ) {
      throw new Error(i18n.t("common:errors.requestTimeout"));
    }
    // Re-throw the error if it's not a network error
    throw error;
  }

  /**
   * Create timeout controller for request
   */
  private createTimeoutController(timeoutMs: number = 30000): {
    controller: AbortController;
    timeoutId: ReturnType<typeof setTimeout>;
    signal: AbortSignal;
  } {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    return { controller, timeoutId, signal: controller.signal };
  }

  /**
   * Parse response body based on content type
   */
  private async parseResponse<T>(response: Response): Promise<T> {
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      return response.json();
    }
    return {} as T;
  }

  /**
   * Build full URL from endpoint
   */
  private buildUrl(endpoint: string): string {
    // Remove leading slash if present to avoid double slashes
    const cleanEndpoint = endpoint.startsWith("/")
      ? endpoint.slice(1)
      : endpoint;
    return `${this.baseUrl}/${cleanEndpoint}`;
  }

  /**
   * Extract error message from error response data
   */
  private extractErrorMessage(errorData: unknown): string | null {
    if (!errorData || typeof errorData !== "object") {
      return null;
    }

        const errorObject = errorData as Record<string, unknown>;

        // Backend sends { success: false, message: "..." } format - prioritize message field
    if (
      typeof errorObject.message === "string" &&
      errorObject.message.trim() !== ""
    ) {
      return errorObject.message;
    }

    // Check nested error object
    if ("error" in errorObject) {
          const nestedError = errorObject.error;
          if (typeof nestedError === "string" && nestedError.trim() !== "") {
        return nestedError;
      }
      if (
        nestedError &&
        typeof nestedError === "object" &&
        "message" in nestedError &&
        typeof (nestedError as { message?: string }).message === "string"
      ) {
            const nestedMessage = (nestedError as { message?: string }).message;
            if (nestedMessage && nestedMessage.trim() !== "") {
          return nestedMessage;
        }
            }
          }

    // Check errors array or object
    if ("errors" in errorObject) {
          const errors = errorObject.errors;
          if (Array.isArray(errors)) {
            const messages = errors
              .map((e) => {
            if (
              typeof e === "object" &&
              e !== null &&
              "message" in e &&
              typeof (e as { message?: string }).message === "string"
            ) {
                  return (e as { message?: string }).message;
                }
                return String(e);
              })
              .filter((msg): msg is string => typeof msg === "string" && msg.trim() !== "");
            if (messages.length > 0) {
          return messages.join(", ");
            }
          } else if (typeof errors === "object" && errors !== null) {
            // Handle object with field errors like { field1: "error1", field2: "error2" }
        const fieldErrors = Object.values(errors).filter(
          (msg): msg is string => typeof msg === "string" && msg.trim() !== ""
        );
            if (fieldErrors.length > 0) {
          return fieldErrors.join(", ");
        }
      }
    }

    return null;
  }

  /**
   * Handle API errors and extract error messages
   */
  private async handleError(response: Response, endpoint?: string): Promise<never> {
    let errorMessage = "An error occurred";
    let errorDetails: unknown = undefined;

    // Try to extract error message from response body
    try {
      const errorData = await response.json();
      const extractedMessage = this.extractErrorMessage(errorData);
      if (extractedMessage) {
        errorMessage = extractedMessage;
      }

      // Extract details if present
      if (
        errorData &&
        typeof errorData === "object" &&
        "details" in errorData
      ) {
        errorDetails = (errorData as { details?: unknown }).details;
      }
    } catch {
      // If response is not JSON, use status text
      errorMessage = response.statusText || `HTTP ${response.status}`;
    }

    // Handle 401 Unauthorized - differentiate between login failures and session expiration
    if (response.status === 401) {
      const isAuthEndpoint =
        endpoint &&
        (endpoint.includes("/auth/login") ||
        endpoint.includes("/auth/forgot-password") || 
        endpoint.includes("/auth/reset-password") ||
          endpoint.includes("/auth/change-password"));
      
      const isPublicEndpoint = endpoint && this.isPublicEndpoint(endpoint);
      
      // Only treat as session expiration if:
      // 1. It's NOT an auth endpoint (login failures should show the actual error)
      // 2. It's NOT a public endpoint (public endpoints should show the actual error)
      // 3. AND we couldn't extract a meaningful error message
      if (
        !isAuthEndpoint &&
        !isPublicEndpoint &&
        errorMessage === "An error occurred"
      ) {
        // Clear auth data for session expiration
        localStorage.removeItem("auth_token");
        localStorage.removeItem("auth_user");
        localStorage.removeItem("remember_me");
        
        // Call unauthorized handler if set
        if (this.onUnauthorized) {
          this.onUnauthorized();
        }
        
        errorMessage = i18n.t("common:errors.sessionExpired");
      }
    }

    const error = new Error(errorMessage) as Error & {
      status: number;
      response: Response;
      details?: unknown;
    };
    error.status = response.status;
    error.response = response;
    if (typeof errorDetails !== "undefined") {
      error.details = errorDetails;
    }
    throw error;
  }

  /**
   * Make a GET request
   */
  async get<T>(endpoint: string, options?: RequestInit): Promise<T> {
    this.validateTokenForRequest(endpoint);
    const headers = this.buildHeaders(options, { endpoint });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "GET",
        headers,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Make a GET request that returns a Blob (for file downloads)
   */
  async getBlob(endpoint: string, options?: RequestInit): Promise<Blob> {
    this.validateTokenForRequest(endpoint);
    const headers = this.buildHeaders(options, {
      omitJsonContentType: true,
      endpoint,
    });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "GET",
        headers,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return response.blob();
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Make a POST request
   */
  async post<T>(
    endpoint: string,
    data?: unknown,
    options?: RequestInit
  ): Promise<T> {
    this.validateTokenForRequest(endpoint);
    const isFormData =
      typeof FormData !== "undefined" && data instanceof FormData;
    const headers = this.buildHeaders(options, {
      omitJsonContentType: isFormData,
      endpoint,
    });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "POST",
        headers,
        body: isFormData
          ? (data as FormData)
          : data
            ? JSON.stringify(data)
            : undefined,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Make a PUT request
   */
  async put<T>(
    endpoint: string,
    data?: unknown,
    options?: RequestInit
  ): Promise<T> {
    this.validateTokenForRequest(endpoint);
    const isFormData =
      typeof FormData !== "undefined" && data instanceof FormData;
    const headers = this.buildHeaders(options, {
      omitJsonContentType: isFormData,
      endpoint,
    });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "PUT",
        headers,
        body: isFormData
          ? (data as FormData)
          : data
            ? JSON.stringify(data)
            : undefined,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Make a PATCH request
   */
  async patch<T>(
    endpoint: string,
    data?: unknown,
    options?: RequestInit
  ): Promise<T> {
    this.validateTokenForRequest(endpoint);
    const isFormData =
      typeof FormData !== "undefined" && data instanceof FormData;
    const headers = this.buildHeaders(options, {
      omitJsonContentType: isFormData,
      endpoint,
    });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "PATCH",
        headers,
        body: isFormData
          ? (data as FormData)
          : data
            ? JSON.stringify(data)
            : undefined,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Make a DELETE request
   */
  async delete<T>(endpoint: string, options?: RequestInit): Promise<T> {
    this.validateTokenForRequest(endpoint);
    const headers = this.buildHeaders(options, { endpoint });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      const response = await fetch(this.buildUrl(endpoint), {
        method: "DELETE",
        headers,
        ...options,
        signal: options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
    } finally {
      clearTimeout(timeoutId);
    }
  }
}

// Export singleton instance
export const apiClient = new ApiClient();
