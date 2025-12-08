import React, { useMemo } from "react";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
  PaginationEllipsis,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { Table } from "@tanstack/react-table";
import { getVisiblePages } from "@/lib/utils/pagination";

interface ServerSidePaginationProps {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
}

interface DataTablePaginationProps<TData> {
  table?: Table<TData>;
  enableRowSelection?: boolean;
  pageSizeOptions?: number[];
  // Server-side pagination props (when table is not provided)
  serverSidePagination?: ServerSidePaginationProps;
}

export function DataTablePagination<TData>({
  table,
  enableRowSelection = false,
  pageSizeOptions = [10, 25, 50, 100],
  serverSidePagination,
}: DataTablePaginationProps<TData>) {
  // Determine if we're using server-side or client-side pagination
  const isServerSide = !!serverSidePagination && !table;

  // Server-side pagination values
  const serverPage = serverSidePagination?.page ?? 1;
  const serverPageSize = serverSidePagination?.pageSize ?? 10;
  const serverTotalItems = serverSidePagination?.totalItems ?? 0;
  const serverTotalPages = serverSidePagination?.totalPages ?? 0;

  // Client-side pagination values (from TanStack Table)
  const paginationState = table?.getState().pagination;
  const clientPageSize = paginationState?.pageSize ?? 10;
  const clientPageIndex = paginationState?.pageIndex ?? 0;
  const filteredRowModel = table?.getFilteredRowModel();
  const clientTotalRows = filteredRowModel?.rows.length ?? 0;
  const clientPageCount = table?.getPageCount() ?? 0;

  // Use server-side or client-side values
  const pageSize = isServerSide ? serverPageSize : clientPageSize;
  const pageIndex = isServerSide ? serverPage - 1 : clientPageIndex; // Convert 1-based to 0-based for calculations
  const totalRows = isServerSide ? serverTotalItems : clientTotalRows;
  const pageCount = isServerSide ? serverTotalPages : clientPageCount;
  const currentPage = isServerSide ? serverPage : pageIndex + 1; // For display (1-based)
  
  const { from, to } = useMemo(() => {
    if (totalRows === 0) return { from: 0, to: 0 };
    if (isServerSide) {
      const from = (serverPage - 1) * serverPageSize + 1;
      const to = Math.min(serverPage * serverPageSize, serverTotalItems);
      return { from, to };
    }
    const from = pageIndex * pageSize + 1;
    const to = Math.min((pageIndex + 1) * pageSize, totalRows);
    return { from, to };
  }, [isServerSide, pageIndex, pageSize, totalRows, serverPage, serverPageSize, serverTotalItems]);

  // Get visible pages for pagination controls
  const visiblePages = useMemo(() => {
    if (isServerSide) {
      return getVisiblePages(serverPage, serverTotalPages);
    }
    // For client-side, use the same logic as server-side but with 0-based index
    const currentPage = pageIndex + 1; // Convert to 1-based for getVisiblePages
    return getVisiblePages(currentPage, pageCount);
  }, [isServerSide, serverPage, serverTotalPages, pageIndex, pageCount]);

  // Handlers for page changes
  const handlePageChange = (newPage: number) => {
    if (isServerSide && serverSidePagination) {
      serverSidePagination.onPageChange(newPage);
    } else if (table) {
      table.setPageIndex(newPage - 1); // Convert 1-based to 0-based
    }
  };

  const handlePageSizeChange = (newPageSize: number) => {
    if (isServerSide && serverSidePagination) {
      serverSidePagination.onPageSizeChange(newPageSize);
    } else if (table) {
      table.setPageSize(newPageSize);
      table.setPageIndex(0);
    }
  };

  const handlePreviousPage = () => {
    if (isServerSide && serverSidePagination && serverPage > 1) {
      serverSidePagination.onPageChange(serverPage - 1);
    } else if (table) {
      table.previousPage();
    }
  };

  const handleNextPage = () => {
    if (isServerSide && serverSidePagination && serverPage < serverTotalPages) {
      serverSidePagination.onPageChange(serverPage + 1);
    } else if (table) {
      table.nextPage();
    }
  };

  const canPreviousPage = isServerSide ? serverPage > 1 : table?.getCanPreviousPage() ?? false;
  const canNextPage = isServerSide ? serverPage < serverTotalPages : table?.getCanNextPage() ?? false;
  const selectedRowCount = table?.getFilteredSelectedRowModel().rows.length ?? 0;

  // Don't show pagination if there are no results (for both server-side and client-side)
  if (totalRows === 0) {
    return null;
  }

  return (
    <div className="flex flex-col gap-4 py-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        {enableRowSelection && table && (
          <div className="text-xs text-muted-foreground sm:text-sm">
            {selectedRowCount} of {totalRows} row(s) selected.
          </div>
        )}
        <div className="flex items-center gap-2">
          <p className="text-xs text-muted-foreground sm:text-sm">Rows per page</p>
          <Select
            value={`${pageSize}`}
            onValueChange={(value) => {
              handlePageSizeChange(Number(value));
            }}
          >
            <SelectTrigger className="h-8 w-[70px]">
              <SelectValue placeholder={String(pageSize)} />
            </SelectTrigger>
            <SelectContent side="top">
              {pageSizeOptions.map((size) => (
                <SelectItem key={size} value={`${size}`}>
                  {size}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <div className="text-xs text-muted-foreground sm:text-sm whitespace-nowrap">
          {totalRows === 0 ? (
            "No results"
          ) : (
            <>
              Showing {from} to {to} of {totalRows} result{totalRows !== 1 ? "s" : ""}
            </>
          )}
        </div>
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                onClick={handlePreviousPage}
                className={
                  !canPreviousPage
                    ? "pointer-events-none opacity-50"
                    : "cursor-pointer"
                }
              />
            </PaginationItem>
            {visiblePages.map((pageNumber, index, array) => {
              const showEllipsisBefore = index > 0 && pageNumber - array[index - 1] > 1;

              return (
                <React.Fragment key={pageNumber}>
                  {showEllipsisBefore && (
                    <PaginationItem>
                      <PaginationEllipsis />
                    </PaginationItem>
                  )}
                  <PaginationItem>
                    <PaginationLink
                      onClick={() => handlePageChange(pageNumber)}
                      isActive={currentPage === pageNumber}
                      className="cursor-pointer"
                    >
                      {pageNumber}
                    </PaginationLink>
                  </PaginationItem>
                </React.Fragment>
              );
            })}
            <PaginationItem>
              <PaginationNext
                onClick={handleNextPage}
                className={
                  !canNextPage
                    ? "pointer-events-none opacity-50"
                    : "cursor-pointer"
                }
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  );
}

