import { useCallback, useState } from "react";
import { clampPage } from "@/lib/utils/pagination";

export type PaginationState = {
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
};

const DEFAULT_PAGINATION: PaginationState = {
  page: 1,
  pageSize: 10,
  totalPages: 0,
  totalItems: 0,
};

export function usePagination(defaultPageSize: number = 10) {
  const [pagination, setPagination] = useState<PaginationState>({
    ...DEFAULT_PAGINATION,
    pageSize: defaultPageSize,
  });

  const handlePageChange = useCallback(
    (newPage: number) => {
      setPagination((prev) => ({
        ...prev,
        page: clampPage(newPage, prev.totalPages),
      }));
    },
    []
  );

  const handlePageSizeChange = useCallback((size: number) => {
    setPagination((prev) => ({
      ...prev,
      page: 1,
      pageSize: size,
    }));
  }, []);

  const updatePagination = useCallback((updates: Partial<PaginationState>) => {
    setPagination((prev) => ({
      ...prev,
      ...updates,
    }));
  }, []);

  const resetPagination = useCallback(() => {
    setPagination({
      ...DEFAULT_PAGINATION,
      pageSize: defaultPageSize,
    });
  }, [defaultPageSize]);

  return {
    pagination,
    setPagination,
    handlePageChange,
    handlePageSizeChange,
    updatePagination,
    resetPagination,
  };
}

