/**
 * Teacher Assignment management types
 */

export type AssignmentStatus = "PLANNED" | "CONFIRMED" | "CANCELLED" | "ON_HOLD";

export interface TeacherAssignment {
  id: number;
  planId: number;
  planTitle: string;
  teacherId: number;
  teacherTitle: string;
  internshipTypeId: number;
  internshipTypeTitle: string;
  subjectId: number;
  subjectTitle: string;
  studentGroupSize: number;
  assignmentStatus: AssignmentStatus;
  isManualOverride: boolean;
  notes: string | null;
  assignedAt: string | null; // ISO string, from LocalDateTime
  createdAt: string;
  updatedAt: string;
}

export interface CreateTeacherAssignmentRequest {
  planId: number;
  teacherId: number;
  internshipTypeId: number;
  subjectId: number;
  studentGroupSize: number;
  assignmentStatus: AssignmentStatus;
  isManualOverride?: boolean;
  notes?: string;
}

export interface UpdateTeacherAssignmentRequest {
  studentGroupSize?: number;
  assignmentStatus?: AssignmentStatus;
  isManualOverride?: boolean;
  notes?: string;
}

export interface TeacherAssignmentResponse {
  success: boolean;
  message: string;
  data: TeacherAssignment;
}

export interface TeacherAssignmentsListResponse {
  success: boolean;
  message: string;
  data: TeacherAssignment[];
}

export interface PaginatedTeacherAssignmentsResponse {
  success: boolean;
  message: string;
  data: {
    items: TeacherAssignment[];
    totalItems: number;
    totalPages: number;
    page: number;
    pageSize: number;
  };
}

export interface TeacherAssignmentsListParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  searchValue?: string;
  teacherId?: number;
  internshipTypeId?: number;
  subjectId?: number;
  assignmentStatus?: AssignmentStatus;
}