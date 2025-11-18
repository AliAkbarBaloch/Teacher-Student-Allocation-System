// components
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

// types
import type { TFunction } from "i18next";

interface PaginationSummary {
  from: number;
  to: number;
}

interface SchoolsPaginationControlsProps {
  paginationSummary: PaginationSummary;
  pagination: {
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
  };
  pageSizeOptions: readonly number[];
  visiblePages: number[];
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  t: TFunction<"schools">;
}

export function SchoolsPaginationControls({
  paginationSummary,
  pagination,
  pageSizeOptions,
  visiblePages,
  onPageChange,
  onPageSizeChange,
  t,
}: SchoolsPaginationControlsProps) {
  if (pagination.totalItems === 0) {
    return null;
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
        <p className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
          {t("pagination.display", {
            from: paginationSummary.from,
            to: paginationSummary.to,
            total: pagination.totalItems,
          })}
        </p>
        <div className="flex items-center gap-2">
          <p className="text-xs text-muted-foreground whitespace-nowrap hidden sm:inline">
            {t("pagination.pageSize")}
          </p>
          <p className="text-xs text-muted-foreground whitespace-nowrap sm:hidden">
            {t("pagination.pageSizeShort")}
          </p>
          <Select value={String(pagination.pageSize)} onValueChange={(value) => onPageSizeChange(Number(value))}>
            <SelectTrigger className="h-8 w-[100px] text-xs sm:text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {pageSizeOptions.map((size) => (
                <SelectItem key={size} value={String(size)}>
                  {size}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex items-center justify-center gap-1 sm:gap-2">
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                onClick={(event) => {
                  event.preventDefault();
                  if (pagination.page > 1) onPageChange(pagination.page - 1);
                }}
                className={pagination.page === 1 ? "pointer-events-none opacity-50" : ""}
              />
            </PaginationItem>
            {visiblePages.map((pageNumber) => (
              <PaginationItem key={pageNumber}>
                <PaginationLink
                  href="#"
                  isActive={pageNumber === pagination.page}
                  onClick={(event) => {
                    event.preventDefault();
                    onPageChange(pageNumber);
                  }}
                >
                  {pageNumber}
                </PaginationLink>
              </PaginationItem>
            ))}
            <PaginationItem>
              <PaginationNext
                href="#"
                onClick={(event) => {
                  event.preventDefault();
                  if (pagination.page < Math.max(pagination.totalPages, 1)) {
                    onPageChange(pagination.page + 1);
                  }
                }}
                className={pagination.page >= Math.max(pagination.totalPages, 1) ? "pointer-events-none opacity-50" : ""}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>
    </div>
  );
}

