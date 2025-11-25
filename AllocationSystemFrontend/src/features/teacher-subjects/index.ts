/**
 * TeacherSubjects feature exports
 */

export { TeacherSubjectDialogs } from "./components/TeacherSubjectDialogs";
export { TeacherSubjectForm } from "./components/TeacherSubjectForm";
export { useTeacherSubjectsPage } from "./hooks/useTeacherSubjectsPage";
export { useTeacherSubjectsColumnConfig } from "./utils/columnConfig";
export { TeacherSubjectService } from "./services/teacherSubjectService";
export type {
  TeacherSubject,
  CreateTeacherSubjectRequest,
  UpdateTeacherSubjectRequest,
  TeacherSubjectResponse,
  TeacherSubjectsListResponse,
  PaginatedTeacherSubjectsResponse,
  TeacherSubjectsListParams,
} from "./types/teacherSubject.types";