import { flexRender, type Table as TanStackTable } from "@tanstack/react-table";
import { Loader2 } from "lucide-react";
import { TableBody, TableCell, TableRow } from "@/components/ui/table";
import type { ColumnConfig } from "@/types/datatable.types";

interface DataTableBodyProps<TData> {
  table: TanStackTable<TData>;
  columnConfig?: ColumnConfig[];
  loading: boolean;
  emptyMessage: string;
  enableRowClick: boolean;
  onRowClick?: (row: TData) => void;
}

/**
 * Renders the table body with loading, empty, and data states
 */
export function DataTableBody<TData>({
  table,
  columnConfig,
  loading,
  emptyMessage,
  enableRowClick,
  onRowClick,
}: DataTableBodyProps<TData>) {
  const rows = table.getRowModel().rows;
  const columns = table.getAllColumns();

  if (loading) {
    return (
      <TableBody>
        <TableRow>
          <TableCell
            colSpan={columns.length}
            className="h-24 text-center px-2 sm:px-4"
          >
            <div className="flex items-center justify-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span className="text-xs sm:text-sm text-muted-foreground">
                Loading...
              </span>
            </div>
          </TableCell>
        </TableRow>
      </TableBody>
    );
  }

  if (!rows.length) {
    return (
      <TableBody>
        <TableRow>
          <TableCell
            colSpan={columns.length}
            className="h-24 text-center text-muted-foreground px-2 sm:px-4 text-xs sm:text-sm"
          >
            {emptyMessage}
          </TableCell>
        </TableRow>
      </TableBody>
    );
  }

  return (
    <TableBody>
      {rows.map((row) => (
        <TableRow
          key={row.id}
          data-state={row.getIsSelected() && "selected"}
          className={`hover:bg-muted/50 ${
            enableRowClick && onRowClick ? "cursor-pointer" : ""
          }`}
          onClick={() => {
            if (enableRowClick && onRowClick) {
              onRowClick(row.original);
            }
          }}
        >
          {row.getVisibleCells().map((cell) => {
            const accessorKey =
              "accessorKey" in cell.column.columnDef
                ? cell.column.columnDef.accessorKey
                : undefined;
            const cellColumnConfig = columnConfig?.find(
              (c) => c.field === accessorKey || c.field === cell.column.id
            );
            const isActionsColumn = cell.column.id === "actions";
            
            // Skip config lookup for actions column (it doesn't have a ColumnConfig)
            const width = isActionsColumn ? undefined : cellColumnConfig?.width;
            const maxWidth = isActionsColumn ? undefined : cellColumnConfig?.maxWidth;
            const enableTruncation =
              isActionsColumn
                ? false
                : cellColumnConfig?.enableTruncation ??
                  (maxWidth !== undefined ? true : false);
            const isFullNameColumn = cellColumnConfig?.field === "fullName";

            // Determine cell className based on column type and config
            let cellClassName = "px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm";
            if (isFullNameColumn) {
              cellClassName += " break-all";
            } else if (isActionsColumn) {
              // Actions column: no special text handling needed
            } else if (enableTruncation && maxWidth) {
              cellClassName += " truncate";
            } else {
              cellClassName += " whitespace-nowrap";
            }

            // Build cell styles
            const cellStyles: React.CSSProperties = {};
            if (isActionsColumn) {
              cellStyles.width = "fit-content";
              cellStyles.minWidth = "fit-content";
            } else {
              if (width) {
                cellStyles.width =
                  typeof width === "number" ? `${width}px` : width;
              }
              if (maxWidth) {
                cellStyles.maxWidth =
                  typeof maxWidth === "number" ? `${maxWidth}px` : maxWidth;
              }
            }

            // Get title text for truncated cells (native browser tooltip)
            const titleText =
              enableTruncation && maxWidth
                ? (() => {
                    const rawValue = cell.getValue();
                    return rawValue !== null && rawValue !== undefined
                      ? String(rawValue)
                      : undefined;
                  })()
                : undefined;

            return (
              <TableCell
                key={cell.id}
                className={cellClassName}
                style={cellStyles}
                title={titleText}
              >
                {flexRender(cell.column.columnDef.cell, cell.getContext())}
              </TableCell>
            );
          })}
        </TableRow>
      ))}
    </TableBody>
  );
}

