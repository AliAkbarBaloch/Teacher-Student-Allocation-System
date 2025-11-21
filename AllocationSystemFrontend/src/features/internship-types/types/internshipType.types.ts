/**
 * Internship Type management types
 */

export interface InternshipType {
  id: number;
  internshipCode: string;
  fullName: string;
  timing: string | null;
  periodType: string | null;
  semester: string | null;
  isSubjectSpecific: boolean;
  priorityOrder: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInternshipTypeRequest {
  internshipCode: string;
  fullName: string;
  timing?: string | null;
  periodType?: string | null;
  semester?: string | null;
  isSubjectSpecific?: boolean;
  priorityOrder?: number | null;
}

export interface UpdateInternshipTypeRequest {
  internshipCode?: string;
  fullName?: string;
  timing?: string | null;
  periodType?: string | null;
  semester?: string | null;
  isSubjectSpecific?: boolean;
  priorityOrder?: number | null;
}

export interface InternshipTypeResponse {
  success: boolean;
  message: string;
  data: InternshipType;
}

export interface InternshipTypesListResponse {
  success: boolean;
  message: string;
  data: InternshipType[];
}

export interface PaginatedInternshipTypesResponse {
  success: boolean;
  message: string;
  data: {
    items: InternshipType[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface InternshipTypesListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
}

/**
 * Predefined internship type codes from acceptance criteria
 */
export const INTERNSHIP_TYPE_CODES = {
  PDP_I: "PDP I",
  PDP_II: "PDP II",
  ZSP: "ZSP",
  SFP: "SFP",
} as const;

/**
 * Timing options
 */
export const TIMING_OPTIONS = [
  { value: "Autumn (lecture-free period)", label: "Autumn (lecture-free period)" },
  { value: "Spring (lecture-free period)", label: "Spring (lecture-free period)" },
  { value: "Winter semester (Wednesdays)", label: "Winter semester (Wednesdays)" },
  { value: "Summer semester (Wednesdays)", label: "Summer semester (Wednesdays)" },
] as const;

/**
 * Period type options
 */
export const PERIOD_TYPE_OPTIONS = [
  { value: "Block", label: "Block" },
  { value: "Wednesday", label: "Wednesday" },
] as const;

/**
 * Semester options
 */
export const SEMESTER_OPTIONS = [
  { value: "1st", label: "1st Semester" },
  { value: "2nd", label: "2nd Semester" },
] as const;


