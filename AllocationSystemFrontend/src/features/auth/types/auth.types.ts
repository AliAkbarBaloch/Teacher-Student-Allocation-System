export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

/**
 * Backend login response structure
 */
export interface LoginResponse {
  token: string;
  tokenType?: string;
  userId: number;
  email: string;
  fullName: string;
  role: string;
}

/**
 * Frontend auth response (transformed from backend)
 */
export interface AuthResponse {
  user: {
    id: string;
    email: string;
    name: string;
    fullName: string;
    role: string;
  };
  token: string;
}

/**
 * User profile from backend
 */
export interface UserProfile {
  id: number;
  email: string;
  fullName: string;
  role: string;
  phoneNumber?: string;
  enabled: boolean;
  accountLocked: boolean;
  accountStatus: string;
  failedLoginAttempts: number;
  lastLoginDate?: string;
  lastPasswordResetDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthError {
  message: string;
  field?: "email" | "password" | "general";
}

export interface AuthState {
  user: AuthResponse["user"] | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: AuthError | null;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateProfileRequest {
  email: string;
  fullName: string;
  phoneNumber?: string;
}

