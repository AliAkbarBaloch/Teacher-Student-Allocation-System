export const SCHOOL_TYPE_VALUES = ["PRIMARY", "MIDDLE"] as const;

export type SchoolType = (typeof SCHOOL_TYPE_VALUES)[number];

export interface School {
  id: number;
  schoolName: string;
  schoolType: SchoolType;
  zoneNumber: number;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  distanceFromCenter?: number | null;
  transportAccessibility?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

// --- Added types below ---

export interface CreateSchoolRequest {
  schoolName: string;
  schoolType: SchoolType;
  zoneNumber: number;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  distanceFromCenter?: number | null;
  transportAccessibility?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  isActive: boolean;
}

export interface UpdateSchoolRequest {
  schoolName?: string;
  schoolType?: SchoolType;
  zoneNumber?: number;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  distanceFromCenter?: number | null;
  transportAccessibility?: string | null;
  contactEmail?: string | null;
  contactPhone?: string | null;
  isActive?: boolean;
}

export interface SchoolResponse {
  success: boolean;
  message: string;
  data: School;
}

export interface SchoolsListResponse {
  success: boolean;
  message: string;
  data: School[];
}

export interface PaginatedSchoolsResponse {
  success: boolean;
  message: string;
  data: {
    items: School[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface SchoolFilters {
  search?: string;
  schoolType?: SchoolType;
  zoneNumber?: number;
  isActive?: boolean;
}

export interface SchoolsListParams extends SchoolFilters {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

// --- Existing types below ---

export interface PaginatedSchoolResponse {
  items: School[];
  totalItems: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

export interface SchoolStatusUpdateRequest {
  isActive: boolean;
}

export type SchoolFormErrors = Partial<
  Record<keyof CreateSchoolRequest, string>
> & {
  general?: string;
};

export interface ApiErrorDetails {
  [key: string]: string | undefined;
}

export type ApiErrorResponse = Error & {
  status?: number;
  details?: unknown;
};

export function isApiError(error: unknown): error is ApiErrorResponse {
  return typeof error === "object" && error !== null && "message" in error;
}
