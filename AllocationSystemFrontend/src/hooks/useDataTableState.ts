import { useState } from "react";
import type {
  SortingState,
  ColumnFiltersState,
  VisibilityState,
} from "@tanstack/react-table";

interface UseDataTableStateOptions {
  defaultPageSize?: number;
}

/**
 * Hook to manage DataTable state (sorting, filtering, pagination, etc.)
 */
export function useDataTableState({
  defaultPageSize = 10,
}: UseDataTableStateOptions = {}) {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = useState({});
  const [pageSize, setPageSize] = useState(defaultPageSize);
  const [pageIndex, setPageIndex] = useState(0);

  return {
    sorting,
    setSorting,
    columnFilters,
    setColumnFilters,
    columnVisibility,
    setColumnVisibility,
    rowSelection,
    setRowSelection,
    pageSize,
    setPageSize,
    pageIndex,
    setPageIndex,
  };
}

