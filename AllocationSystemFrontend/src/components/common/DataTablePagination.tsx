import React from "react";
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

interface DataTablePaginationProps<TData> {
  table: Table<TData>;
  enableRowSelection?: boolean;
  pageSizeOptions?: number[];
}

export function DataTablePagination<TData>({
  table,
  enableRowSelection = false,
  pageSizeOptions = [10, 25, 50, 100],
}: DataTablePaginationProps<TData>) {
  const pageSize = table.getState().pagination.pageSize;
  const pageIndex = table.getState().pagination.pageIndex;

  return (
    <div className="flex flex-col gap-4 py-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        {enableRowSelection && (
          <div className="text-xs text-muted-foreground sm:text-sm">
            {table.getFilteredSelectedRowModel().rows.length} of{" "}
            {table.getFilteredRowModel().rows.length} row(s) selected.
          </div>
        )}
        <div className="flex items-center gap-2">
          <p className="text-xs text-muted-foreground sm:text-sm">Rows per page</p>
          <Select
            value={`${pageSize}`}
            onValueChange={(value) => {
              table.setPageSize(Number(value));
              table.setPageIndex(0);
            }}
          >
            <SelectTrigger className="h-8 w-[70px]">
              <SelectValue placeholder={pageSize} />
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
        <div className="text-xs text-muted-foreground sm:text-sm">
          Showing {pageIndex * pageSize + 1} to{" "}
          {Math.min(
            (pageIndex + 1) * pageSize,
            table.getFilteredRowModel().rows.length
          )}{" "}
          of {table.getFilteredRowModel().rows.length} results
        </div>
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                onClick={() => {
                  table.previousPage();
                }}
                className={
                  !table.getCanPreviousPage()
                    ? "pointer-events-none opacity-50"
                    : "cursor-pointer"
                }
              />
            </PaginationItem>
            {Array.from({ length: table.getPageCount() }, (_, i) => i)
              .filter((pageIdx) => {
                const totalPages = table.getPageCount();
                return (
                  pageIdx === 0 ||
                  pageIdx === totalPages - 1 ||
                  (pageIdx >= pageIndex - 1 && pageIdx <= pageIndex + 1)
                );
              })
              .map((pageIdx, index, array) => {
                const showEllipsisBefore = index > 0 && pageIdx - array[index - 1] > 1;

                return (
                  <React.Fragment key={pageIdx}>
                    {showEllipsisBefore && (
                      <PaginationItem>
                        <PaginationEllipsis />
                      </PaginationItem>
                    )}
                    <PaginationItem>
                      <PaginationLink
                        onClick={() => table.setPageIndex(pageIdx)}
                        isActive={pageIndex === pageIdx}
                        className="cursor-pointer"
                      >
                        {pageIdx + 1}
                      </PaginationLink>
                    </PaginationItem>
                  </React.Fragment>
                );
              })}
            <PaginationItem>
              <PaginationNext
                onClick={() => {
                  table.nextPage();
                }}
                className={
                  !table.getCanNextPage()
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

