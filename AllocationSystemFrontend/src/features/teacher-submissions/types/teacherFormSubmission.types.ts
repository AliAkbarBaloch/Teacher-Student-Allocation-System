export interface TeacherFormSubmission {
  id: number;
  teacherId: number;
  teacherFirstName: string;
  teacherLastName: string;
  teacherEmail: string;
  yearId: number;
  yearName: string;
  formToken: string | null;
  submittedAt: string | null;
  // Submission data fields (distinct instead of JSON)
  schoolId: number | null;
  employmentStatus: string | null;
  notes: string | null;
  subjectIds: number[];
  internshipTypePreference: string | null;
  internshipCombinations: string[];
  semesterAvailability: string[];
  availabilityOptions: string[];
  isProcessed: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PaginatedTeacherFormSubmissionResponse {
  items: TeacherFormSubmission[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface TeacherFormSubmissionFilters {
  teacherId?: number;
  yearId?: number;
  isProcessed?: boolean;
}

export interface TeacherFormSubmissionListParams extends TeacherFormSubmissionFilters {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

export interface UpdateSubmissionStatusRequest {
  isProcessed: boolean;
}

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

export type TeacherFormSubmissionApiResponse = ApiResponse<TeacherFormSubmission>;
export type PaginatedTeacherFormSubmissionApiResponse = ApiResponse<PaginatedTeacherFormSubmissionResponse>;

export interface FormLinkGenerateRequest {
  teacherId: number;
  yearId: number;
}

export interface FormLinkResponse {
  formToken: string;
  formUrl: string;
  teacherId: number;
  teacherName: string;
  teacherEmail: string;
  yearId: number;
  yearName: string;
}

export interface PublicFormSubmissionRequest {
  schoolId: number;
  notes: string;
  subjectIds: number[];
  internshipTypeIds: number[];
}

export type FormLinkApiResponse = ApiResponse<FormLinkResponse>;

