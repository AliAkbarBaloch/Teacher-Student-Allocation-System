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
            const width = cellColumnConfig?.width;
            const maxWidth = cellColumnConfig?.maxWidth;
            const isFullNameColumn = cellColumnConfig?.field === "fullName";

            return (
              <TableCell
                key={cell.id}
                className={`px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm ${
                  isFullNameColumn ? "break-all" : "whitespace-nowrap"
                }`}
                style={{
                  minWidth: "fit-content",
                  ...(width && {
                    width: typeof width === "number" ? `${width}px` : width,
                  }),
                  ...(maxWidth && {
                    maxWidth:
                      typeof maxWidth === "number"
                        ? `${maxWidth}px`
                        : maxWidth,
                  }),
                }}
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

