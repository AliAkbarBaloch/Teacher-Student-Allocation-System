/**
 * TeacherAssignments feature exports
 */

export { TeacherAssignmentDialogs } from "./components/TeacherAssignmentDialogs";
export { TeacherAssignmentForm } from "./components/TeacherAssignmentForm";
export { useTeacherAssignmentsPage } from "./hooks/useTeacherAssignmentsPage";
export { useTeacherAssignmentsColumnConfig } from "./utils/columnConfig";
export { TeacherAssignmentService } from "./services/teacherAssginmentService";
export type {
  TeacherAssignment,
  CreateTeacherAssignmentRequest,
  UpdateTeacherAssignmentRequest,
  TeacherAssignmentResponse,
  TeacherAssignmentsListResponse,
  PaginatedTeacherAssignmentsResponse,
  TeacherAssignmentsListParams,
  AssignmentStatus,
} from "./types/teacherAssignment.types";