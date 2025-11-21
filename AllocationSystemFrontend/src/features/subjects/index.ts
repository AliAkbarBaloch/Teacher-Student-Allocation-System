/**
 * Subjects feature exports
 */

export { SubjectDialogs } from "./components/SubjectDialogs";
export { SubjectForm } from "./components/SubjectForm";
export { useSubjectsPage } from "./hooks/useSubjectsPage";
export { useSubjectsColumnConfig } from "./utils/columnConfig";
export { SubjectService } from "./services/subjectService";
export type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
  SubjectResponse,
  SubjectsListResponse,
  PaginatedSubjectsResponse,
  SubjectsListParams,
} from "./types/subject.types";

