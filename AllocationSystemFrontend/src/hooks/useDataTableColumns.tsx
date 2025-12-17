import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { ChevronsUpDown, ArrowUp, ArrowDown } from "lucide-react";
import type { ColumnConfig } from "@/types/datatable.types";

/**
 * Hook to generate Table columns from ColumnConfig
 * Handles formatting, sorting, and alignment
 */
export function useDataTableColumns<TData, TValue>(
  columnConfig?: ColumnConfig[]
): ColumnDef<TData, TValue>[] {
  return useMemo(() => {
    if (!columnConfig) return [];

    return columnConfig.map((config) => {
      const column: ColumnDef<TData, TValue> = {
        accessorKey: config.field as keyof TData,
        enableSorting: config.enableSorting ?? true,
        ...(config.filterFn && { filterFn: config.filterFn }),
        header: ({ column: headerColumn }) => {
          const canSort = headerColumn.getCanSort();
          const sortDirection = headerColumn.getIsSorted();

          const headerContent =
            config.align === "right" ? (
              <div className="text-right">{config.title}</div>
            ) : config.align === "center" ? (
              <div className="text-center">{config.title}</div>
            ) : (
              config.title
            );

          if (!canSort) {
            return headerContent;
          }

          return (
            <div
              className={`flex items-center gap-1 sm:gap-2 ${
                config.align === "right"
                  ? "justify-end"
                  : config.align === "center"
                  ? "justify-center"
                  : "justify-start"
              } cursor-pointer select-none hover:text-foreground`}
              onClick={headerColumn.getToggleSortingHandler()}
            >
              {typeof headerContent === "string" ? (
                <span>{headerContent}</span>
              ) : (
                headerContent
              )}
              {sortDirection === "asc" ? (
                <ArrowUp className="h-3 w-3 sm:h-4 sm:w-4 shrink-0" />
              ) : sortDirection === "desc" ? (
                <ArrowDown className="h-3 w-3 sm:h-4 sm:w-4 shrink-0" />
              ) : (
                <ChevronsUpDown className="h-3 w-3 sm:h-4 sm:w-4 opacity-50 shrink-0" />
              )}
            </div>
          );
        },
        cell: ({ row }) => {
          const value = row.getValue(config.field);

          // Handle function format first
          if (typeof config.format === "function") {
            const formatted = config.format(value, row.original);
            return (
              <div
                className={
                  config.align === "right"
                    ? "text-right"
                    : config.align === "center"
                    ? "text-center"
                    : ""
                }
              >
                {formatted}
              </div>
            );
          }

          // Handle formatting based on format type
          if (config.format === "currency") {
            const amount =
              typeof value === "number" ? value : parseFloat(String(value));
            if (isNaN(amount)) return <div>{String(value || "")}</div>;
            const formatted = new Intl.NumberFormat("en-US", {
              style: "currency",
              currency: config.currencyCode || "USD",
            }).format(amount);
            return (
              <div
                className={`${
                  config.align === "right"
                    ? "text-right"
                    : config.align === "center"
                    ? "text-center"
                    : ""
                } font-medium`}
              >
                {formatted}
              </div>
            );
          }

          if (config.format === "number") {
            const num =
              typeof value === "number" ? value : parseFloat(String(value));
            if (isNaN(num)) return <div>{String(value || "")}</div>;
            return (
              <div
                className={
                  config.align === "right"
                    ? "text-right"
                    : config.align === "center"
                    ? "text-center"
                    : ""
                }
              >
                {num.toLocaleString()}
              </div>
            );
          }

          if (config.format === "date" && value) {
            try {
              const date =
                value instanceof Date ? value : new Date(String(value));
              if (isNaN(date.getTime()))
                return <div>{String(value || "")}</div>;
              return (
                <div
                  className={
                    config.align === "right"
                      ? "text-right"
                      : config.align === "center"
                      ? "text-center"
                      : ""
                  }
                >
                  {date.toLocaleDateString()}
                </div>
              );
            } catch {
              return <div>{String(value || "")}</div>;
            }
          }

          let displayValue = String(value ?? "");

          if (config.format === "capitalize") {
            displayValue =
              displayValue.charAt(0).toUpperCase() +
              displayValue.slice(1).toLowerCase();
          } else if (config.format === "lowercase") {
            displayValue = displayValue.toLowerCase();
          } else if (config.format === "uppercase") {
            displayValue = displayValue.toUpperCase();
          }

          return (
            <div
              className={
                config.align === "right"
                  ? "text-right"
                  : config.align === "center"
                  ? "text-center"
                  : ""
              }
            >
              {displayValue}
            </div>
          );
        },
      };

      return column;
    });
  }, [columnConfig]);
}
