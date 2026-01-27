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

/**
 * Props for server-side pagination configuration.
 */
interface ServerSidePaginationProps {
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
}

/**
 * Props for the DataTablePagination component.
 */
interface DataTablePaginationProps<TData> {
  table?: Table<TData>;
  enableRowSelection?: boolean;
  pageSizeOptions?: readonly number[] | number[];
  // Server-side pagination props (when table is not provided)
  serverSidePagination?: ServerSidePaginationProps;
}

/**
 * Sub-component for selecting the number of rows per page.
 */
function RowsPerPageSelector({
  pageSize,
  onPageSizeChange,
  pageSizeOptions,
}: {
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  pageSizeOptions: readonly number[] | number[];
}) {
  return (
    <div className="flex items-center gap-2">
      <p className="text-xs text-muted-foreground sm:text-sm">Rows per page</p>
      <Select
        value={`${pageSize}`}
        onValueChange={(value) => onPageSizeChange(Number(value))}
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
  );
}

/**
 * Sub-component for rendering the pagination links and navigation arrows.
 */
function PageNavControls({
  currentPage,
  pageCount,
  visiblePages,
  onPageChange,
  onPreviousPage,
  onNextPage,
  canPreviousPage,
  canNextPage,
}: {
  currentPage: number;
  pageCount: number;
  visiblePages: number[];
  onPageChange: (page: number) => void;
  onPreviousPage: () => void;
  onNextPage: () => void;
  canPreviousPage: boolean;
  canNextPage: boolean;
}) {
  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          <PaginationPrevious
            onClick={onPreviousPage}
            className={!canPreviousPage ? "pointer-events-none opacity-50" : "cursor-pointer"}
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
                  onClick={() => onPageChange(pageNumber)}
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
            onClick={onNextPage}
            className={!canNextPage ? "pointer-events-none opacity-50" : "cursor-pointer"}
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
}

/**
 * Handles pagination for the DataTable, supporting both client-side (via TanStack Table)
 * and server-side pagination modes.
 */
export function DataTablePagination<TData>({
  table,
  enableRowSelection = false,
  pageSizeOptions = [10, 25, 50, 100],
  serverSidePagination,
}: DataTablePaginationProps<TData>) {
  const isServerSide = !!serverSidePagination && !table;

  // Server-side values
  const serverPage = serverSidePagination?.page ?? 1;
  const serverPageSize = serverSidePagination?.pageSize ?? 10;
  const serverTotalItems = serverSidePagination?.totalItems ?? 0;
  const serverTotalPages = serverSidePagination?.totalPages ?? 0;

  // Client-side values
  const paginationState = table?.getState().pagination;
  const clientPageSize = paginationState?.pageSize ?? 10;
  const clientPageIndex = paginationState?.pageIndex ?? 0;
  const filteredRowModel = table?.getFilteredRowModel();
  const clientTotalRows = filteredRowModel?.rows.length ?? 0;
  const clientPageCount = table?.getPageCount() ?? 0;

  // Current values
  const pageSize = isServerSide ? serverPageSize : clientPageSize;
  const pageIndex = isServerSide ? serverPage - 1 : clientPageIndex;
  const totalRows = isServerSide ? serverTotalItems : clientTotalRows;
  const pageCount = isServerSide ? serverTotalPages : clientPageCount;
  const currentPage = isServerSide ? serverPage : pageIndex + 1;

  const { from, to } = useMemo(() => {
    if (totalRows === 0) {
      return { from: 0, to: 0 };
    }
    if (isServerSide) {
      const fromVal = (serverPage - 1) * serverPageSize + 1;
      const toVal = Math.min(serverPage * serverPageSize, serverTotalItems);
      return { from: fromVal, to: toVal };
    }
    const fromVal = pageIndex * pageSize + 1;
    const toVal = Math.min((pageIndex + 1) * pageSize, totalRows);
    return { from: fromVal, to: toVal };
  }, [isServerSide, pageIndex, pageSize, totalRows, serverPage, serverPageSize, serverTotalItems]);

  const visiblePages = useMemo(() => {
    if (isServerSide) {
      return getVisiblePages(serverPage, serverTotalPages);
    }
    return getVisiblePages(pageIndex + 1, pageCount);
  }, [isServerSide, serverPage, serverTotalPages, pageIndex, pageCount]);

  const handlePageChange = (newPage: number) => {
    if (isServerSide && serverSidePagination) {
      serverSidePagination.onPageChange(newPage);
    } else if (table) {
      table.setPageIndex(newPage - 1);
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
        <RowsPerPageSelector
          pageSize={pageSize}
          onPageSizeChange={handlePageSizeChange}
          pageSizeOptions={pageSizeOptions}
        />
      </div>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <div className="text-xs text-muted-foreground sm:text-sm whitespace-nowrap">
          {totalRows === 0 ? "No results" : `Showing ${from} to ${to} of ${totalRows} results`}
        </div>
        <PageNavControls
          currentPage={currentPage}
          pageCount={pageCount}
          visiblePages={visiblePages}
          onPageChange={handlePageChange}
          onPreviousPage={handlePreviousPage}
          onNextPage={handleNextPage}
          canPreviousPage={canPreviousPage}
          canNextPage={canNextPage}
        />
      </div>
    </div>
  );
}
