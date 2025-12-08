import { useCallback, useMemo } from "react";
import { getPaginationSummary } from "@/lib/utils/pagination";
import type { CreditHourTrackingListParams } from "../types/creditHourTracking.types";
import type { CreditHourTracking } from "../types/creditHourTracking.types";

interface UseCreditHourTrackingPaginationParams {
  pagination: {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  };
  filteredEntries: CreditHourTracking[];
  hasClientFilters: boolean;
  filterParams: CreditHourTrackingListParams;
  loadEntries: (params: CreditHourTrackingListParams) => Promise<void>;
}

/**
 * Hook for managing pagination logic and handlers
 */
export function useCreditHourTrackingPagination({
  pagination,
  filteredEntries,
  hasClientFilters,
  filterParams,
  loadEntries,
}: UseCreditHourTrackingPaginationParams) {
  // Calculate pagination summary based on filtered results
  const paginationSummary = useMemo(() => {
    const totalToDisplay = hasClientFilters ? filteredEntries.length : pagination.totalItems;
    const pageSize = hasClientFilters ? filteredEntries.length : pagination.pageSize;
    const page = hasClientFilters ? 1 : pagination.page;
    
    return getPaginationSummary(page, pageSize, totalToDisplay);
  }, [pagination.page, pagination.pageSize, pagination.totalItems, filteredEntries.length, hasClientFilters]);

  const handlePageChange = useCallback((newPage: number) => {
    loadEntries({
      ...filterParams,
      page: newPage,
      pageSize: pagination.pageSize,
      sortBy: "creditBalance",
      sortOrder: "desc",
    });
  }, [filterParams, pagination.pageSize, loadEntries]);

  const handlePageSizeChange = useCallback((size: number) => {
    loadEntries({
      ...filterParams,
      page: 1,
      pageSize: size,
      sortBy: "creditBalance",
      sortOrder: "desc",
    });
  }, [filterParams, loadEntries]);

  return {
    paginationSummary,
    handlePageChange,
    handlePageSizeChange,
  };
}
