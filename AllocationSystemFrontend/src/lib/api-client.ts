import { API_BASE_URL } from "@/config";
import i18n from "@/lib/i18n";

/**
 * Configuration object for HTTP requests
 */
interface RequestConfig {
  /** The API endpoint to make the request to */
  endpoint: string;
  /** HTTP method (GET, POST, PUT, PATCH, DELETE) */
  method: string;
  /** Optional data to send in the request body */
  data?: unknown;
  /** Additional fetch options */
  options?: RequestInit;
  /** Whether to omit the JSON Content-Type header (e.g., for FormData) */
  omitJsonContentType?: boolean;
}

/**
 * Extended Error object with additional HTTP response information
 */
interface ErrorResponse extends Error {
  /** HTTP status code from the response */
  status: number;
  /** The original Response object */
  response: Response;
  /** Additional error details from the response body */
  details?: unknown;
}

/**
 * API Client for making HTTP requests to the backend
 */
class ApiClient {
  private baseUrl: string;
  private onUnauthorized?: () => void;

  constructor() {
    this.baseUrl = API_BASE_URL || "http://localhost:8080/api";
  }

  setUnauthorizedHandler(handler: () => void) {
    this.onUnauthorized = handler;
  }

  private getAuthToken(): string | null {
    return localStorage.getItem("auth_token");
  }

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

      const exp = payload.exp * 1000;
      return Date.now() >= exp;
    } catch {
      return true;
    }
  }

  private isAuthEndpoint(endpoint: string): boolean {
    return endpoint.includes("/auth/login") ||
           endpoint.includes("/auth/forgot-password") || 
           endpoint.includes("/auth/reset-password");
  }

  private isPublicEndpoint(endpoint: string): boolean {
    return endpoint.includes("/public/teacher-form-submission/");
  }

  private handleExpiredToken(): void {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
    if (this.onUnauthorized) {
      this.onUnauthorized();
    }
  }

  private validateTokenForRequest(endpoint: string): void {
    if (this.isAuthEndpoint(endpoint) || this.isPublicEndpoint(endpoint)) {
      return;
    }
    
    const token = this.getAuthToken();
    if (token && this.isTokenExpired(token)) {
      this.handleExpiredToken();
      throw new Error(i18n.t("common:errors.sessionExpired"));
    }
  }

  private buildHeaders(config: RequestConfig): Record<string, string> {
    const token = this.getAuthToken();
    let headers: Record<string, string> = {};
    
    if (config.options?.headers && typeof config.options.headers === "object" && !(config.options.headers instanceof Headers)) {
      headers = { ...(config.options.headers as Record<string, string>) };
    }

    if (!config.omitJsonContentType && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    if (token && !this.isPublicEndpoint(config.endpoint)) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
  }

  private handleNetworkError(error: unknown): never {
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      throw new Error(i18n.t("common:errors.networkError"));
    }
    if (error instanceof Error && (error.name === "AbortError" || error.name === "TimeoutError")) {
      throw new Error(i18n.t("common:errors.requestTimeout"));
    }
    throw error;
  }

  private createTimeoutController(timeoutMs: number = 30000) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    return { controller, timeoutId, signal: controller.signal };
  }

  private async parseResponse<T>(response: Response): Promise<T> {
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      return response.json();
    }
    return {} as T;
  }

  private buildUrl(endpoint: string): string {
    let cleanEndpoint = endpoint;
    if (endpoint.startsWith("/")) {
      cleanEndpoint = endpoint.slice(1);
    }
    return `${this.baseUrl}/${cleanEndpoint}`;
  }

  private extractErrorMessage(errorData: unknown): string | null {
    if (!errorData || typeof errorData !== "object") {
      return null;
    }

    const errorObject = errorData as Record<string, unknown>;

    if (typeof errorObject.message === "string" && errorObject.message.trim() !== "") {
      return errorObject.message;
    }

    if ("error" in errorObject) {
      const nestedError = errorObject.error;
      if (typeof nestedError === "string" && nestedError.trim() !== "") {
        return nestedError;
      }
      if (nestedError && typeof nestedError === "object" && "message" in nestedError) {
        const nestedMessage = (nestedError as { message?: string }).message;
        if (nestedMessage && nestedMessage.trim() !== "") {
          return nestedMessage;
        }
      }
    }

    if ("errors" in errorObject) {
      const errors = errorObject.errors;
      if (Array.isArray(errors)) {
        const messages = errors
          .map(e => {
            if (typeof e === "object" && e !== null && "message" in e) {
              return (e as { message?: string }).message;
            } else {
              return String(e);
            }
          })
          .filter((msg): msg is string => typeof msg === "string" && msg.trim() !== "");
        if (messages.length > 0) {
          return messages.join(", ");
        }
      } else if (typeof errors === "object" && errors !== null) {
        const fieldErrors = Object.values(errors).filter(
          msg => typeof msg === "string" && msg.trim() !== ""
        );
        if (fieldErrors.length > 0) {
          return fieldErrors.join(", ");
        }
      }
    }

    return null;
  }

  private handleSessionExpiration(): void {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
    localStorage.removeItem("remember_me");
    if (this.onUnauthorized) {
      this.onUnauthorized();
    }
  }

  private async handleError(response: Response, endpoint?: string): Promise<never> {
    let errorMessage = "An error occurred";
    let errorDetails: unknown = undefined;

    try {
      const errorData = await response.json();
      const extractedMessage = this.extractErrorMessage(errorData);
      if (extractedMessage) {
        errorMessage = extractedMessage;
      }
      if (errorData && typeof errorData === "object" && "details" in errorData) {
        errorDetails = (errorData as { details?: unknown }).details;
      }
    } catch {
      errorMessage = response.statusText || `HTTP ${response.status}`;
    }

    if (response.status === 401) {
      const isAuthEndpoint = endpoint && (
        endpoint.includes("/auth/login") ||
        endpoint.includes("/auth/forgot-password") ||
        endpoint.includes("/auth/reset-password") ||
        endpoint.includes("/auth/change-password")
      );
      
      const isPublicEndpoint = endpoint && this.isPublicEndpoint(endpoint);
      
      if (!isAuthEndpoint && !isPublicEndpoint && errorMessage === "An error occurred") {
        this.handleSessionExpiration();
        errorMessage = i18n.t("common:errors.sessionExpired");
      }
    }

    const error = new Error(errorMessage) as ErrorResponse;
    error.status = response.status;
    error.response = response;
    if (typeof errorDetails !== "undefined") {
      error.details = errorDetails;
    }
    throw error;
  }

  private prepareRequestBody(data: unknown, isFormData: boolean): BodyInit | undefined {
    if (isFormData) {
      return data as FormData;
    } else if (data) {
      return JSON.stringify(data);
    } else {
      return undefined;
    }
  }

  private async executeRequest<T>(config: RequestConfig): Promise<T> {
    this.validateTokenForRequest(config.endpoint);
    const isFormData = typeof FormData !== "undefined" && config.data instanceof FormData;
    const headers = this.buildHeaders({ ...config, omitJsonContentType: isFormData });
    const { timeoutId, signal } = this.createTimeoutController();

    try {
      let body: BodyInit | undefined;
      if (config.method === "GET" || config.method === "DELETE") {
        body = undefined;
      } else {
        body = this.prepareRequestBody(config.data, isFormData);
      }

      const response = await fetch(this.buildUrl(config.endpoint), {
        method: config.method,
        headers,
        body,
        ...config.options,
        signal: config.options?.signal || signal,
      });

      if (!response.ok) {
        await this.handleError(response, config.endpoint);
      }

      return this.parseResponse<T>(response);
    } catch (error) {
      this.handleNetworkError(error);
      return Promise.reject(error) as unknown as T;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  async get<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.executeRequest<T>({ endpoint, method: "GET", options });
  }

  async getBlob(endpoint: string, options?: RequestInit): Promise<Blob> {
    this.validateTokenForRequest(endpoint);
    const headers = this.buildHeaders({ endpoint, method: "GET", omitJsonContentType: true });
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
      return Promise.reject(error) as unknown as Blob;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  async post<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    return this.executeRequest<T>({ endpoint, method: "POST", data, options });
  }

  async put<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    return this.executeRequest<T>({ endpoint, method: "PUT", data, options });
  }

  async patch<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    return this.executeRequest<T>({ endpoint, method: "PATCH", data, options });
  }

  async delete<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.executeRequest<T>({ endpoint, method: "DELETE", options });
  }
}

export const apiClient = new ApiClient();