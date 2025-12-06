/**
 * AcademicYear management types
*/

export interface ZoneConstraint {
  id: number;
  zoneNumber: number;
  internshipTypeId: number;
  internshipTypeCode?: string;
  internshipTypeName?: string;
  isAllowed: boolean;
  description?: string | null;
  createdAt: string; // ISO string
  updatedAt?: string | null;
}

export interface CreateZoneConstraintRequest {
  zoneNumber: number;
  internshipTypeId: number;
  isAllowed: boolean;
  description?: string | null;
}

export interface UpdateZoneConstraintRequest {
  zoneNumber?: number;
  internshipTypeId?: number;
  isAllowed?: boolean;
  description?: string | null;
}

export interface ZoneConstraintResponse {
  success: boolean;
  message: string;
  data: ZoneConstraint;
}

export interface ZoneConstraintsListResponse {
  success: boolean;
  message: string;
  data: ZoneConstraint[];
}

export interface PaginatedZoneConstraintsResponse {
  success: boolean;
  message: string;
  data: {
    items: ZoneConstraint[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface ZoneConstraintsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}