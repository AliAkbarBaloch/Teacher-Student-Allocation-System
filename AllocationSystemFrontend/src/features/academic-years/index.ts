/**
 * AcademicYears feature exports
 */

export { AcademicYearDialogs } from "./components/AcademicYearDialogs";
export { AcademicYearForm } from "./components/AcademicYearForm";
export { useAcademicYearsPage } from "./hooks/useSubjectsPage";
export { useAcademicYearsColumnConfig } from "./utils/columnConfig";
export { AcademicYearService } from "./services/academicYearService";
export type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
  AcademicYearResponse,
  AcademicYearsListResponse,
  PaginatedAcademicYearsResponse,
  AcademicYearsListParams,
} from "./types/academicYear.types";