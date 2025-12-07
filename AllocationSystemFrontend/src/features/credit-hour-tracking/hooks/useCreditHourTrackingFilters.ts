import { useState, useCallback } from "react";

export interface CreditHourTrackingFilters {
  academicYearId?: number;
  teacherId?: number;
  minBalance?: number;
}

export function useCreditHourTrackingFilters() {
  const [filters, setFilters] = useState<CreditHourTrackingFilters>({});
  const [teacherSearch, setTeacherSearch] = useState("");

  const handleFilterChange = useCallback((newFilters: Partial<CreditHourTrackingFilters>) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
  }, []);

  const handleResetFilters = useCallback(() => {
    setFilters({});
    setTeacherSearch("");
  }, []);

  return {
    filters,
    teacherSearch,
    setTeacherSearch,
    handleFilterChange,
    handleResetFilters,
  };
}
