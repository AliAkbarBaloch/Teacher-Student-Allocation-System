/**
 * Subject Categories feature exports
 */

export { SubjectCategoryDialogs } from "./components/SubjectCategoryDialogs";
export { SubjectCategoryForm } from "./components/SubjectCategoryForm";
export { useSubjectCategoriesPage } from "./hooks/useSubjectCategoriesPage";
export { useSubjectCategoriesColumnConfig } from "./utils/columnConfig";
export { SubjectCategoryService } from "./services/subjectCategoryService";
export type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
  SubjectCategoryResponse,
  SubjectCategoriesListResponse,
  PaginatedSubjectCategoriesResponse,
  SubjectCategoriesListParams,
} from "./types/subjectCategory.types";

