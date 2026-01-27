import React from "react";
import { flexRender, type Table as TanStackTable, type Cell } from "@tanstack/react-table";
import { Loader2 } from "lucide-react";
import { TableBody, TableCell, TableRow } from "@/components/ui/table";
import type { ColumnConfig } from "@/types/datatable.types";

/**
 * Props for the DataTableBody component.
 */
interface DataTableBodyProps<TData> {
  table: TanStackTable<TData>;
  columnConfig?: ColumnConfig[];
  loading: boolean;
  emptyMessage: string;
  enableRowClick: boolean;
  onRowClick?: (row: TData) => void;
}

/**
 * Renders a loading row that spans all columns.
 */
function LoadingRow({ colSpan }: { colSpan: number }) {
  return (
    <TableRow>
      <TableCell colSpan={colSpan} className="h-24 text-center px-2 sm:px-4">
        <div className="flex items-center justify-center gap-2">
          <Loader2 className="h-4 w-4 animate-spin" />
          <span className="text-xs sm:text-sm text-muted-foreground">Loading...</span>
        </div>
      </TableCell>
    </TableRow>
  );
}

/**
 * Renders an empty row with a message when no data is available.
 */
function EmptyRow({ colSpan, message }: { colSpan: number; message: string }) {
  return (
    <TableRow>
      <TableCell
        colSpan={colSpan}
        className="h-24 text-center text-muted-foreground px-2 sm:px-4 text-xs sm:text-sm"
      >
        {message}
      </TableCell>
    </TableRow>
  );
}

interface DataCellProps<TData> {
  cell: Cell<TData, unknown>;
  columnConfig?: ColumnConfig[];
}

/**
 * Renders a data cell with appropriate styling and truncation.
 */
function DataCell<TData>({ cell, columnConfig }: DataCellProps<TData>) {
  const accessorKey = "accessorKey" in cell.column.columnDef ? cell.column.columnDef.accessorKey : undefined;
  const cellColumnConfig = columnConfig?.find((c) => c.field === accessorKey || c.field === cell.column.id);
  const isActionsColumn = cell.column.id === "actions";

  const width = isActionsColumn ? undefined : cellColumnConfig?.width;
  const maxWidth = isActionsColumn ? undefined : cellColumnConfig?.maxWidth;
  const enableTruncation = isActionsColumn ? false : cellColumnConfig?.enableTruncation ?? (maxWidth !== undefined);
  const isFullNameColumn = cellColumnConfig?.field === "fullName";

  let cellClassName = "px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm";
  if (isFullNameColumn) {
    cellClassName += " break-all";
  } else if (!isActionsColumn) {
    cellClassName += enableTruncation && maxWidth ? " truncate" : " whitespace-nowrap";
  }

  const cellStyles: React.CSSProperties = isActionsColumn
    ? { width: "fit-content", minWidth: "fit-content" }
    : {
      ...(width && { width: typeof width === "number" ? `${width}px` : width }),
      ...(maxWidth && { maxWidth: typeof maxWidth === "number" ? `${maxWidth}px` : maxWidth }),
    };

  const titleText = enableTruncation && maxWidth ? String(cell.getValue() ?? "") : undefined;

  return (
    <TableCell key={cell.id} className={cellClassName} style={cellStyles} title={titleText}>
      {flexRender(cell.column.columnDef.cell, cell.getContext())}
    </TableCell>
  );
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
  const columns = table.getAllColumns();
  const rows = table.getRowModel().rows;

  if (loading) {
    return (
      <TableBody>
        <LoadingRow colSpan={columns.length} />
      </TableBody>
    );
  }

  if (!rows.length) {
    return (
      <TableBody>
        <EmptyRow colSpan={columns.length} message={emptyMessage} />
      </TableBody>
    );
  }

  return (
    <TableBody>
      {rows.map((row) => (
        <TableRow
          key={row.id}
          data-state={row.getIsSelected() && "selected"}
          className={`hover:bg-muted/50 ${enableRowClick && onRowClick ? "cursor-pointer" : ""}`}
          onClick={() => enableRowClick && onRowClick?.(row.original)}
        >
          {row.getVisibleCells().map((cell) => (
            <DataCell key={cell.id} cell={cell} columnConfig={columnConfig} />
          ))}
        </TableRow>
      ))}
    </TableBody>
  );
}
