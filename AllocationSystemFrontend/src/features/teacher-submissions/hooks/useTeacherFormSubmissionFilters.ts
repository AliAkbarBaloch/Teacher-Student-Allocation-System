import { useState, useCallback, useMemo } from "react";
import type { TeacherFormSubmissionFilters } from "../types/teacherFormSubmission.types";

export function useTeacherFormSubmissionFilters() {
  const [filters, setFilters] = useState<TeacherFormSubmissionFilters>({
    teacherId: undefined,
    yearId: undefined,
    isProcessed: undefined,
  });

  const [teacherSearch, setTeacherSearch] = useState("");

  const handleFilterChange = useCallback((newFilters: Partial<TeacherFormSubmissionFilters>) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
  }, []);

  const handleResetFilters = useCallback(() => {
    setFilters({
      teacherId: undefined,
      yearId: undefined,
      isProcessed: undefined,
    });
    setTeacherSearch("");
  }, []);

  const hasActiveFilters = useMemo(() => {
    return (
      filters.teacherId !== undefined ||
      filters.yearId !== undefined ||
      filters.isProcessed !== undefined ||
      teacherSearch.trim() !== ""
    );
  }, [filters, teacherSearch]);

  return {
    filters,
    teacherSearch,
    setTeacherSearch,
    handleFilterChange,
    handleResetFilters,
    hasActiveFilters,
  };
}


