import { useState, useMemo, useCallback, useEffect } from "react";

interface UseClientPaginationOptions<T> {
  data: T[];
  pageSize?: number;
}

interface UseClientPaginationReturn<T> {
  paginatedData: T[];
  currentPage: number;
  totalPages: number;
  handlePreviousPage: () => void;
  handleNextPage: () => void;
  setCurrentPage: (page: number) => void;
}

/**
 * Hook for client-side pagination of an array
 * Useful when you have all data loaded and want to paginate it in the UI
 */
export function useClientPagination<T>({
  data,
  pageSize = 50,
}: UseClientPaginationOptions<T>): UseClientPaginationReturn<T> {
  const [currentPage, setCurrentPage] = useState(1);

  const totalPages = Math.ceil(data.length / pageSize);

  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    return data.slice(start, end);
  }, [data, currentPage, pageSize]);

  const handlePreviousPage = useCallback(() => {
    setCurrentPage((prev) => Math.max(1, prev - 1));
  }, []);

  const handleNextPage = useCallback(() => {
    setCurrentPage((prev) => Math.min(totalPages, prev + 1));
  }, [totalPages]);

  const handleSetCurrentPage = useCallback((page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)));
  }, [totalPages]);

  // Reset to page 1 when data length changes significantly
  useEffect(() => {
    if (currentPage > totalPages && totalPages > 0) {
      setCurrentPage(1);
    }
  }, [data.length, totalPages, currentPage]);

  return {
    paginatedData,
    currentPage,
    totalPages,
    handlePreviousPage,
    handleNextPage,
    setCurrentPage: handleSetCurrentPage,
  };
}
