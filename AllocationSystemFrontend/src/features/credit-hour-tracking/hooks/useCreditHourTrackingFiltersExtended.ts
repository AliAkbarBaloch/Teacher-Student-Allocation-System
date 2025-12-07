import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useDebounce } from "@/hooks/useDebounce";
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";
import { useCreditHourTrackingFilters } from "./useCreditHourTrackingFilters";
import type { CreditHourTracking } from "../types/creditHourTracking.types";

/**
 * Extended filters hook that includes:
 * - Base filter state management
 * - Academic year loading and route handling
 * - Debouncing
 * - API parameter conversion
 * - Client-side filtering logic
 */
export function useCreditHourTrackingFiltersExtended(entries: CreditHourTracking[] = []) {
  const { yearId: routeYearId } = useParams<{ yearId?: string }>();
  const filters = useCreditHourTrackingFilters();
  const [currentYearId, setCurrentYearId] = useState<number | undefined>(
    routeYearId ? Number(routeYearId) : undefined
  );

  // Load academic years and find current year
  useEffect(() => {
    let isMounted = true;
    const loadAcademicYears = async () => {
      try {
        const years = await AcademicYearService.getAll();
        if (!isMounted) return;
        
        // Find current year if not set from route
        if (!currentYearId && years.length > 0) {
          const currentYear = years.find((y) => y.isLocked === false) || years[years.length - 1];
          if (currentYear) {
            setCurrentYearId(currentYear.id);
            filters.handleFilterChange({ academicYearId: currentYear.id });
          }
        }
      } catch (error) {
        console.error("Failed to load academic years:", error);
      }
    };
    loadAcademicYears();
    
    return () => {
      isMounted = false;
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Use route yearId or currentYearId for filtering
  const effectiveYearId = useMemo(() => {
    return routeYearId ? Number(routeYearId) : (currentYearId || filters.filters.academicYearId);
  }, [routeYearId, currentYearId, filters.filters.academicYearId]);

  // Debounce numeric filters
  const debouncedMinBalance = useDebounce(filters.filters.minBalance, 500);

  // Convert filter state to API params (only for server-side filters)
  const filterParams = useMemo(() => {
    const params: {
      academicYearId?: number;
      teacherId?: number;
      searchValue?: string;
    } = {};

    if (effectiveYearId) {
      params.academicYearId = effectiveYearId;
    } else if (filters.filters.academicYearId !== undefined) {
      params.academicYearId = filters.filters.academicYearId;
    }

    if (filters.filters.teacherId !== undefined) {
      params.teacherId = filters.filters.teacherId;
    }

    return params;
  }, [effectiveYearId, filters.filters.academicYearId, filters.filters.teacherId]);

  const handleYearChange = useCallback(
    (value?: number) => {
      filters.handleFilterChange({ academicYearId: value });
      if (!routeYearId) {
        setCurrentYearId(value);
      }
    },
    [filters, routeYearId]
  );

  const handleTeacherChange = useCallback(
    (value?: number) => {
      filters.handleFilterChange({ teacherId: value });
    },
    [filters]
  );

  const handleTeacherSearchChange = useCallback(
    (value: string) => {
      // Clear teacherId when user manually changes the search text
      if (filters.filters.teacherId) {
        filters.handleFilterChange({ teacherId: undefined });
      }
      filters.setTeacherSearch(value);
    },
    [filters]
  );

  const handleResetFilters = useCallback(() => {
    filters.handleResetFilters();
    if (!routeYearId && currentYearId) {
      filters.handleFilterChange({ academicYearId: currentYearId });
    }
  }, [filters, routeYearId, currentYearId]);

  // Apply client-side filtering
  const filteredEntries = useMemo(() => {
    let result = entries.filter((e): e is CreditHourTracking => 
      Boolean(e && e.teacherName && e.academicYearTitle)
    );

    // Filter by teacher name/email (client-side since backend doesn't support it)
    if (filters.teacherSearch.trim() && !filters.filters.teacherId) {
      const searchLower = filters.teacherSearch.toLowerCase();
      result = result.filter((e) => 
        e.teacherName?.toLowerCase().includes(searchLower) ?? false
      );
    }
    
    // If teacherId is set, filter by that specific teacher
    if (filters.filters.teacherId) {
      result = result.filter((e) => e.teacherId === filters.filters.teacherId);
    }

    // Filter by debounced balance value
    if (debouncedMinBalance !== undefined) {
      result = result.filter((e) => (e.creditBalance ?? 0) >= debouncedMinBalance);
    }

    return result;
  }, [entries, filters.teacherSearch, filters.filters.teacherId, debouncedMinBalance]);

  // Check if client-side filtering is active
  const hasClientFilters = useMemo(() => {
    return (
      (filters.teacherSearch.trim() && !filters.filters.teacherId) ||
      filters.filters.minBalance !== undefined
    );
  }, [filters.teacherSearch, filters.filters.teacherId, filters.filters.minBalance]);

  return {
    ...filters,
    effectiveYearId,
    debouncedMinBalance,
    filterParams,
    handleYearChange,
    handleTeacherChange,
    handleTeacherSearchChange,
    handleResetFilters,
    filteredEntries,
    hasClientFilters,
  };
}

export type UseCreditHourTrackingFiltersExtendedReturn = ReturnType<typeof useCreditHourTrackingFiltersExtended>;
