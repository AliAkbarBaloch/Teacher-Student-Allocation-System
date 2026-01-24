export type UserRole = "USER" | "ADMIN" | "MODERATOR";

export type AccountStatus =
    | "ACTIVE"
    | "INACTIVE"
    | "SUSPENDED"
    | "PENDING_VERIFICATION";

export interface User {
    id: number;
    roleId: number;
    email: string;
    fullName: string;
    role: UserRole;
    phoneNumber?: string;

    enabled: boolean;
    isActive?: boolean;

    accountLocked: boolean;
    accountStatus: AccountStatus;

    failedLoginAttempts: number;
    loginAttempt?: number;

    lastLoginDate?: string;
    lastPasswordResetDate?: string;
    passwordUpdateDate?: string;

    createdAt?: string;
    updatedAt?: string;
}

export interface CreateUserRequest {
    email: string;
    password: string; // required on create
    fullName: string;
    role: UserRole;   // required
    roleId?: number;
    phoneNumber?: string;
    enabled?: boolean;
    isActive?: boolean;
    accountStatus?: AccountStatus;
}

export interface UpdateUserRequest {
    email?: string;
    fullName?: string;
    role?: UserRole;
    roleId?: number;
    phoneNumber?: string;
    enabled?: boolean;
    isActive?: boolean;
    accountStatus?: AccountStatus;
}

export interface PasswordResetRequest {
    newPassword: string;
    confirmPassword?: string;
}

export interface UsersListParams {
    role?: UserRole;
    status?: AccountStatus;
    enabled?: boolean;
    search?: string;
    page?: number; // backend is 0-based
    size?: number;
    sortBy?: string;
    sortDirection?: "ASC" | "DESC";
}

export interface SpringPage<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number; // 0-based
    first?: boolean;
    last?: boolean;
}
