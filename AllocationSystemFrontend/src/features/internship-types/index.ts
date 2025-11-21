// Export types
export type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
  InternshipTypeResponse,
  InternshipTypesListResponse,
  PaginatedInternshipTypesResponse,
  InternshipTypesListParams,
} from "./types/internshipType.types";

export {
  INTERNSHIP_TYPE_CODES,
  TIMING_OPTIONS,
  PERIOD_TYPE_OPTIONS,
  SEMESTER_OPTIONS,
} from "./types/internshipType.types";

// Export service
export { InternshipTypeService } from "./services/internshipTypeService";

// Export hooks
export { useInternshipTypesPage } from "./hooks/useInternshipTypesPage";

// Export utilities
export { useInternshipTypesColumnConfig } from "./utils/columnConfig";

// Export components
export { InternshipTypeForm } from "./components/InternshipTypeForm";

