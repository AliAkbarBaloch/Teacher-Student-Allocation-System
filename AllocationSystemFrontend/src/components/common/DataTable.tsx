import React from "react";

// react table
import {
  type ColumnDef,
  type ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  type SortingState,
  useReactTable,
  type VisibilityState,
} from "@tanstack/react-table";
import { MoreHorizontal, Eye, Pencil, Trash2, ChevronsUpDown, ArrowUp, ArrowDown, Loader2 } from "lucide-react";

// components
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type {
  DataTableActions,
  DataTableProps,
  ColumnConfig,
} from "@/types/datatable.types";
import { DataTableDialog } from "./DataTableDialog";
import { DataTableToolbar } from "./DataTableToolbar";
import { DataTablePagination } from "./DataTablePagination";

// Re-export types for convenience
export type { ColumnConfig, DataTableActions, DataTableProps };

export function DataTable<TData = Record<string, unknown>, TValue = unknown>({
  columns,
  columnConfig,
  data,
  searchKey,
  searchPlaceholder = "Search...",
  enableSearch = true,
  enableColumnVisibility = true,
  enablePagination = true,
  enableRowSelection = false,
  enableRowClick = true,
  actions,
  actionsHeader = "Actions",
  loading = false,
  error = null,
  emptyMessage = "No results found.",
  pageSizeOptions = [10, 25, 50, 100],
  defaultPageSize = 10,
  validateOnUpdate,
  disableInternalDialog = false,
}: DataTableProps<TData, TValue>) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    []
  );
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});
  const [pageSize, setPageSize] = React.useState(defaultPageSize);
  const [pageIndex, setPageIndex] = React.useState(0);
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
  const [selectedRow, setSelectedRow] = React.useState<TData | null>(null);
  const [editingRow, setEditingRow] = React.useState<Partial<TData>>({});
  const [isEditing, setIsEditing] = React.useState(false);
  const [validationError, setValidationError] = React.useState<string | null>(null);
  const [isSaving, setIsSaving] = React.useState(false);

  // Clear validation error when entering edit mode
  React.useEffect(() => {
    if (isEditing) {
      setValidationError(null);
    }
  }, [isEditing]);

  // Handler to open dialog with row data
  const handleViewRow = React.useCallback((row: TData) => {
    if (disableInternalDialog) {
      // If internal dialog is disabled, just call the callback
      actions?.onView?.(row);
      return;
    }
    setSelectedRow(row);
    setEditingRow({ ...row });
    setIsEditing(false);
    setDialogOpen(true);
    actions?.onView?.(row);
  }, [actions, disableInternalDialog]);

  // Handler to open dialog in edit mode
  const handleEditRow = React.useCallback((row: TData) => {
    if (disableInternalDialog) {
      // If internal dialog is disabled, just call the callback
      actions?.onEdit?.(row);
      return;
    }
    setSelectedRow(row);
    setEditingRow({ ...row });
    setIsEditing(true);
    setValidationError(null);
    setDialogOpen(true);
    actions?.onEdit?.(row);
  }, [actions, disableInternalDialog]);

  // Handler to open delete confirmation
  const handleDeleteClick = React.useCallback((row: TData) => {
    if (disableInternalDialog) {
      // If internal dialog is disabled, just call the callback
      actions?.onDelete?.(row);
      return;
    }
    setSelectedRow(row);
    setDeleteDialogOpen(true);
  }, [actions, disableInternalDialog]);

  // Handler to confirm delete
  const handleConfirmDelete = React.useCallback(() => {
    if (selectedRow) {
      actions?.onDelete?.(selectedRow);
      setDeleteDialogOpen(false);
      setDialogOpen(false);
      setIsEditing(false);
      setSelectedRow(null);
      setEditingRow({});
      setValidationError(null);
    }
  }, [selectedRow, actions]);

  // Handler to save updates
  const handleSave = React.useCallback(async () => {
    if (selectedRow && actions?.onUpdate) {
      setIsSaving(true);
      setValidationError(null);
      
      try {
        // Run validation if provided
        if (validateOnUpdate) {
          const validationResult = await validateOnUpdate(editingRow as TData);
          if (validationResult) {
            setValidationError(validationResult);
            setIsSaving(false);
            return;
          }
        }
        
        await actions.onUpdate(editingRow as TData);
        setDialogOpen(false);
        setIsEditing(false);
        setSelectedRow(null);
        setEditingRow({});
        setValidationError(null);
      } catch (err) {
        setValidationError(err instanceof Error ? err.message : "An error occurred while saving");
      } finally {
        setIsSaving(false);
      }
    }
  }, [selectedRow, editingRow, actions, validateOnUpdate]);

  // Handler to close dialog
  const handleCloseDialog = React.useCallback((open: boolean) => {
    if (!open) {
      // Only close the main dialog if delete dialog is not open
      // This prevents the delete dialog from being closed when transitioning
      if (!deleteDialogOpen) {
        setDialogOpen(false);
        setIsEditing(false);
        setSelectedRow(null);
        setEditingRow({});
        setValidationError(null);
      } else {
        // If delete dialog is open, just close the main dialog but keep delete dialog state
        setDialogOpen(false);
      }
    }
  }, [deleteDialogOpen]);

  // Convert simple column config to full ColumnDef if columnConfig is provided
  const generatedColumns = React.useMemo<ColumnDef<TData, TValue>[]>(() => {
    if (!columnConfig) return [];
    
    return columnConfig.map((config) => {
      const column: ColumnDef<TData, TValue> = {
        accessorKey: config.field as keyof TData,
        enableSorting: config.enableSorting ?? true,
        header: ({ column: headerColumn }) => {
          const canSort = headerColumn.getCanSort();
          const sortDirection = headerColumn.getIsSorted();
          
          const headerContent = config.align === "right" 
            ? <div className="text-right">{config.title}</div>
          : config.align === "center"
            ? <div className="text-center">{config.title}</div>
            : config.title;
          
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
          
          // Handle function format first (before string format checks)
          if (typeof config.format === "function") {
            const formatted = config.format(value, row.original);
            return (
              <div className={config.align === "right" ? "text-right" : config.align === "center" ? "text-center" : ""}>
                {formatted}
              </div>
            );
          }
          
          // Handle formatting based on format type
          if (config.format === "currency") {
            const amount = typeof value === "number" ? value : parseFloat(String(value));
            if (isNaN(amount)) return <div>{String(value || "")}</div>;
            const formatted = new Intl.NumberFormat("en-US", {
              style: "currency",
              currency: config.currencyCode || "USD",
            }).format(amount);
            return (
              <div className={`${config.align === "right" ? "text-right" : config.align === "center" ? "text-center" : ""} font-medium`}>
                {formatted}
              </div>
            );
          }
          
          if (config.format === "number") {
            const num = typeof value === "number" ? value : parseFloat(String(value));
            if (isNaN(num)) return <div>{String(value || "")}</div>;
            return (
              <div className={config.align === "right" ? "text-right" : config.align === "center" ? "text-center" : ""}>
                {num.toLocaleString()}
              </div>
            );
          }
          
          if (config.format === "date" && value) {
            try {
            const date = value instanceof Date ? value : new Date(String(value));
              if (isNaN(date.getTime())) return <div>{String(value || "")}</div>;
            return (
              <div className={config.align === "right" ? "text-right" : config.align === "center" ? "text-center" : ""}>
                {date.toLocaleDateString()}
              </div>
            );
            } catch {
              return <div>{String(value || "")}</div>;
            }
          }
          
          let displayValue = String(value ?? "");
          
          if (config.format === "capitalize") {
            displayValue = displayValue.charAt(0).toUpperCase() + displayValue.slice(1).toLowerCase();
          } else if (config.format === "lowercase") {
            displayValue = displayValue.toLowerCase();
          } else if (config.format === "uppercase") {
            displayValue = displayValue.toUpperCase();
          }
          
          return (
            <div className={config.align === "right" ? "text-right" : config.align === "center" ? "text-center" : ""}>
              {displayValue}
            </div>
          );
        },
      };
      
      return column;
    });
  }, [columnConfig]);

  // Use provided columns or generated columns
  // If neither is provided, throw an error
  if (!columns && !columnConfig) {
    throw new Error("DataTable requires either 'columns' or 'columnConfig' prop");
  }
  
  const baseColumns: ColumnDef<TData, TValue>[] = (columns || generatedColumns) as ColumnDef<TData, TValue>[];

  // Combine columns with actions column if it exists
  const tableColumns = React.useMemo<ColumnDef<TData, TValue>[]>(() => {
    if (!actions) return baseColumns;
    
    const actionsCol: ColumnDef<TData, TValue> = {
      id: "actions",
      enableHiding: true,
      enableSorting: false,
      header: actionsHeader,
      cell: ({ row }) => {
        const rowData = row.original;

        return (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="h-7 w-7 sm:h-8 sm:w-8 p-0">
                <span className="sr-only">Open menu</span>
                <MoreHorizontal className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {actions.onView && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    handleViewRow(rowData);
                  }}
                  className="cursor-pointer"
                >
                  <Eye className="mr-2 h-4 w-4" />
                  {actions.labels?.view || "View"}
                </DropdownMenuItem>
              )}
              {actions.onEdit && (
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    handleEditRow(rowData);
                  }}
                  className="cursor-pointer"
                >
                  <Pencil className="mr-2 h-4 w-4" />
                  {actions.labels?.edit || "Edit"}
                </DropdownMenuItem>
              )}
              {actions.customActions && actions.customActions.length > 0 && (
                <>
                  {(actions.onView || actions.onEdit) && <DropdownMenuSeparator />}
                  {actions.customActions.map((customAction, index) => (
                    <React.Fragment key={index}>
                      {customAction.separator && index > 0 && <DropdownMenuSeparator />}
                      <DropdownMenuItem
                        onClick={(e) => {
                          e.stopPropagation();
                          customAction.onClick(rowData);
                        }}
                        className={`cursor-pointer ${customAction.className || ""}`}
                      >
                        {customAction.icon && <span className="mr-2 h-4 w-4 flex items-center">{customAction.icon}</span>}
                        {typeof customAction.label === "function" ? customAction.label(rowData) : customAction.label}
                      </DropdownMenuItem>
                    </React.Fragment>
                  ))}
                </>
              )}
              {actions.onDelete && (
                <>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteClick(rowData);
                    }}
                    className="cursor-pointer text-destructive focus:text-destructive"
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    {actions.labels?.delete || "Delete"}
                  </DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        );
      },
    };
    
    return [...baseColumns, actionsCol] as ColumnDef<TData, TValue>[];
  }, [baseColumns, actions, actionsHeader, handleViewRow, handleEditRow, handleDeleteClick]);

  const table = useReactTable({
    data,
    columns: tableColumns,
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onColumnVisibilityChange: setColumnVisibility,
    onPaginationChange: (updater) => {
      if (typeof updater === "function") {
        const newPagination = updater({ pageIndex, pageSize });
        setPageIndex(newPagination.pageIndex);
        setPageSize(newPagination.pageSize);
      } else {
        setPageIndex(updater.pageIndex);
        setPageSize(updater.pageSize);
      }
    },
    initialState: {
      pagination: {
        pageSize,
        pageIndex: 0,
      },
    },
    ...(enableRowSelection && {
      onRowSelectionChange: setRowSelection,
      enableRowSelection: true,
    }),
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      pagination: {
        pageIndex,
        pageSize,
      },
      ...(enableRowSelection && { rowSelection }),
    },
  });

  return (
    <div className="w-full">
      <DataTableToolbar
        table={table}
        enableSearch={enableSearch}
        searchKey={searchKey}
        searchPlaceholder={searchPlaceholder}
        enableColumnVisibility={enableColumnVisibility}
      />
      
      {error && (
        <div className="mb-4 p-3 sm:p-4 text-xs sm:text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          {error}
        </div>
      )}
      <div className="w-full overflow-x-auto scroll-smooth -mx-2 sm:mx-0">
        <div className="w-full align-middle rounded-md border">
          <Table className="w-full">
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    const accessorKey = 'accessorKey' in header.column.columnDef ? header.column.columnDef.accessorKey : undefined;
                    const headerColumnConfig = columnConfig?.find(c => c.field === accessorKey || c.field === header.id);
                    const width = headerColumnConfig?.width;
                    const maxWidth = headerColumnConfig?.maxWidth;
                    return (
                      <TableHead 
                        key={header.id} 
                        className="whitespace-nowrap px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm font-medium"
                        style={{ 
                          minWidth: "fit-content",
                          ...(width && { width: typeof width === "number" ? `${width}px` : width }),
                          ...(maxWidth && { maxWidth: typeof maxWidth === "number" ? `${maxWidth}px` : maxWidth }),
                        }}
                      >
                        {header.isPlaceholder
                          ? null
                          : flexRender(
                              header.column.columnDef.header,
                              header.getContext()
                            )}
                      </TableHead>
                    );
                  })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell
                  colSpan={tableColumns.length}
                  className="h-24 text-center px-2 sm:px-4"
                >
                  <div className="flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span className="text-xs sm:text-sm text-muted-foreground">Loading...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && "selected"}
                  className={`hover:bg-muted/50 ${enableRowClick && actions ? "cursor-pointer" : ""}`}
                  onClick={() => {
                    if (enableRowClick && actions) {
                      handleViewRow(row.original);
                    }
                  }}
                >
                  {row.getVisibleCells().map((cell) => {
                    const accessorKey = 'accessorKey' in cell.column.columnDef ? cell.column.columnDef.accessorKey : undefined;
                    const cellColumnConfig = columnConfig?.find(c => c.field === accessorKey || c.field === cell.column.id);
                    const width = cellColumnConfig?.width;
                    const maxWidth = cellColumnConfig?.maxWidth;
                    const isFullNameColumn = cellColumnConfig?.field === "fullName";
                    return (
                      <TableCell 
                        key={cell.id} 
                        className={`px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm ${isFullNameColumn ? "break-all" : "whitespace-nowrap"}`}
                        style={{ 
                          minWidth: "fit-content",
                          ...(width && { width: typeof width === "number" ? `${width}px` : width }),
                          ...(maxWidth && { maxWidth: typeof maxWidth === "number" ? `${maxWidth}px` : maxWidth }),
                        }}
                      >
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext()
                        )}
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={tableColumns.length}
                  className="h-24 text-center text-muted-foreground px-2 sm:px-4 text-xs sm:text-sm"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        </div>
      </div>
      {enablePagination && (
        <DataTablePagination
          table={table}
          enableRowSelection={enableRowSelection}
          pageSizeOptions={pageSizeOptions}
        />
      )}

      {/* View/Edit Dialog - Only render if internal dialog is enabled */}
      {actions && !disableInternalDialog && (
        <DataTableDialog
          open={dialogOpen}
          onOpenChange={handleCloseDialog}
          selectedRow={selectedRow}
          editingRow={editingRow}
          isEditing={isEditing}
          setIsEditing={setIsEditing}
          setEditingRow={setEditingRow}
          columnConfig={columnConfig}
          actions={actions}
          onSave={handleSave}
          onDeleteClick={() => setDeleteDialogOpen(true)}
          deleteDialogOpen={deleteDialogOpen}
          setDeleteDialogOpen={setDeleteDialogOpen}
          onConfirmDelete={handleConfirmDelete}
          validationError={validationError}
          isSaving={isSaving}
        />
      )}
    </div>
  );
}
