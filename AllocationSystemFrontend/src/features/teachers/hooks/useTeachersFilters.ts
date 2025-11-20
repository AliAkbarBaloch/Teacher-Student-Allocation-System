import { useCallback, useMemo, useState } from "react";
import { useDebounce } from "@/hooks/useDebounce";
import { SEARCH_DEBOUNCE_MS } from "@/lib/constants/app";
import type { TeacherFilters, EmploymentStatus } from "../types/teacher.types";

export function useTeachersFilters() {
  const [searchInput, setSearchInput] = useState("");
  const [selectedSchoolId, setSelectedSchoolId] = useState<number | undefined>(undefined);
  const [selectedEmploymentStatus, setSelectedEmploymentStatus] = useState<EmploymentStatus | undefined>(undefined);
  const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all");
  const [isSearchInputLoading, setIsSearchInputLoading] = useState(false);

  const debouncedSearch = useDebounce(searchInput, SEARCH_DEBOUNCE_MS);

  const filters: TeacherFilters = useMemo(
    () => ({
      search: debouncedSearch || undefined,
      schoolId: selectedSchoolId,
      employmentStatus: selectedEmploymentStatus,
      isActive: statusFilter === "all" ? undefined : statusFilter === "active",
    }),
    [debouncedSearch, selectedSchoolId, selectedEmploymentStatus, statusFilter]
  );

  const handleSearchChange = useCallback((value: string, onResetPage?: () => void) => {
    setSearchInput(value);
    setIsSearchInputLoading(true);
    onResetPage?.();
  }, []);

  const handleSchoolIdChange = useCallback((value?: number, onResetPage?: () => void) => {
    setSelectedSchoolId(value);
    onResetPage?.();
  }, []);

  const handleEmploymentStatusChange = useCallback((value?: EmploymentStatus, onResetPage?: () => void) => {
    setSelectedEmploymentStatus(value);
    onResetPage?.();
  }, []);

  const handleStatusFilterChange = useCallback((value: "all" | "active" | "inactive", onResetPage?: () => void) => {
    setStatusFilter(value);
    onResetPage?.();
  }, []);

  const handleResetFilters = useCallback((onResetPage?: () => void) => {
    setSearchInput("");
    setSelectedSchoolId(undefined);
    setSelectedEmploymentStatus(undefined);
    setStatusFilter("all");
    setIsSearchInputLoading(false);
    onResetPage?.();
  }, []);

  return {
    // Filter values
    searchInput,
    selectedSchoolId,
    selectedEmploymentStatus,
    statusFilter,
    isSearchInputLoading,
    setIsSearchInputLoading,
    // Computed filters
    filters,
    // Handlers
    handleSearchChange,
    handleSchoolIdChange,
    handleEmploymentStatusChange,
    handleStatusFilterChange,
    handleResetFilters,
  };
}

