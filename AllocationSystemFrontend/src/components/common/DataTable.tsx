import React, { useCallback } from "react";
import {
  type ColumnDef,
  type Updater,
  type PaginationState,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
  type TableOptions,
} from "@tanstack/react-table";
import { Table, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type {
  DataTableActions,
  DataTableProps,
  ColumnConfig,
} from "@/types/datatable.types";
import { DataTableDialog } from "./DataTableDialog";
import { DataTableToolbar } from "./DataTableToolbar";
import { DataTablePagination } from "./DataTablePagination";
import { DataTableBody } from "./DataTableBody";
import { createActionsColumn } from "./DataTableActionsColumn";
import { useDataTableColumns } from "@/hooks/useDataTableColumns";
import { useDataTableState } from "@/hooks/useDataTableState";
import { useDataTableDialogs } from "@/hooks/useDataTableDialogs";

// Re-export types for convenience
export type { ColumnConfig, DataTableActions, DataTableProps };

const FIT_CONTENT = "fit-content";

/**
 * Calculates the styles for a table head cell based on its configuration.
 */
function getTableHeadStyles(
  isActionsColumn: boolean,
  width?: string | number,
  maxWidth?: string | number
): React.CSSProperties {
  if (isActionsColumn) {
    return {
      width: FIT_CONTENT,
      minWidth: FIT_CONTENT,
    };
  }

  return {
    minWidth: FIT_CONTENT,
    ...(width && {
      width: typeof width === "number" ? `${width}px` : width,
    }),
    ...(maxWidth && {
      maxWidth: typeof maxWidth === "number" ? `${maxWidth}px` : maxWidth,
    }),
  };
}

/**
 * Main DataTable component - refactored to use extracted hooks and components
 * Reduced from ~600 lines to ~250 lines for better maintainability
 */
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
  serverSidePagination,
  validateOnUpdate,
  disableInternalDialog = false,
  tableLayout = "auto",
  onRowClick
}: DataTableProps<TData, TValue>) {
  // Extract state management to hook
  const tableState = useDataTableState({ defaultPageSize });

  // Extract dialog management to hook
  const dialogs = useDataTableDialogs({
    actions,
    disableInternalDialog,
    validateOnUpdate,
  });

  // Extract column generation to hook
  const generatedColumns = useDataTableColumns<TData, TValue>(columnConfig);

  // Validate that either columns or columnConfig is provided
  if (!columns && !columnConfig) {
    throw new Error("DataTable requires either 'columns' or 'columnConfig' prop");
  }

  // Combine base columns with actions column if needed
  const baseColumns: ColumnDef<TData, TValue>[] =
    (columns || generatedColumns) as ColumnDef<TData, TValue>[];

  const tableColumns = React.useMemo<ColumnDef<TData, TValue>[]>(() => {
    if (!actions) {
      return baseColumns;
    }
    return [
      ...baseColumns,
      createActionsColumn<TData, TValue>({
        actions,
        actionsHeader,
        onView: dialogs.handleViewRow,
        onEdit: dialogs.handleEditRow,
        onDelete: dialogs.handleDeleteClick,
      }),
    ];
  }, [baseColumns, actions, actionsHeader, dialogs]);

  // Memoize pagination change handler to ensure stable reference
  const handlePaginationChange = useCallback(
    (updater: Updater<PaginationState>) => {
      if (typeof updater === "function") {
        const currentState = {
          pageIndex: tableState.pageIndex,
          pageSize: tableState.pageSize,
        };
        const newPagination = updater(currentState);
        tableState.setPageIndex(newPagination.pageIndex);
        tableState.setPageSize(newPagination.pageSize);
      } else {
        tableState.setPageIndex(updater.pageIndex);
        tableState.setPageSize(updater.pageSize);
      }
    },
    [tableState]
  );

  // Build table options to avoid large ternary structures
  const tableOptions: TableOptions<TData> = {
    data,
    columns: tableColumns,
    onSortingChange: tableState.setSorting,
    onColumnFiltersChange: tableState.setColumnFilters,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onColumnVisibilityChange: tableState.setColumnVisibility,
    state: {
      sorting: tableState.sorting,
      columnFilters: tableState.columnFilters,
      columnVisibility: tableState.columnVisibility,
    },
  };

  if (enablePagination) {
    tableOptions.getPaginationRowModel = getPaginationRowModel();
    tableOptions.onPaginationChange = handlePaginationChange;
    tableOptions.initialState = {
      pagination: {
        pageSize: defaultPageSize,
        pageIndex: 0,
      },
    };
    if (tableOptions.state) {
      tableOptions.state.pagination = {
        pageIndex: tableState.pageIndex,
        pageSize: tableState.pageSize,
      };
    }
  }

  if (enableRowSelection) {
    tableOptions.onRowSelectionChange = tableState.setRowSelection;
    tableOptions.enableRowSelection = true;
    if (tableOptions.state) {
      tableOptions.state.rowSelection = tableState.rowSelection;
    }
  }

  // Initialize TanStack Table
  const table = useReactTable(tableOptions);

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
          <Table
            className="w-full"
            style={{
              tableLayout: tableLayout === "fixed" ? "fixed" : "auto",
            }}
          >
            <TableHeader>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    const accessorKey =
                      "accessorKey" in header.column.columnDef
                        ? header.column.columnDef.accessorKey
                        : undefined;
                    const headerColumnConfig = columnConfig?.find(
                      (c) => c.field === accessorKey || c.field === header.id
                    );
                    const isActionsColumn = header.id === "actions";

                    return (
                      <TableHead
                        key={header.id}
                        className="whitespace-nowrap px-2 sm:px-4 py-2 sm:py-3 text-xs sm:text-sm font-medium"
                        style={getTableHeadStyles(
                          isActionsColumn,
                          headerColumnConfig?.width,
                          headerColumnConfig?.maxWidth
                        )}
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

            <DataTableBody
              table={table}
              columnConfig={columnConfig}
              loading={loading}
              emptyMessage={emptyMessage}
              enableRowClick={enableRowClick}
              onRowClick={onRowClick ?? dialogs.handleViewRow}
            />
          </Table>
        </div>
      </div>

      {(enablePagination || serverSidePagination) && (
        <DataTablePagination
          table={enablePagination ? table : undefined}
          enableRowSelection={enableRowSelection}
          pageSizeOptions={pageSizeOptions}
          serverSidePagination={serverSidePagination}
        />
      )}

      {/* View/Edit Dialog - Only render if internal dialog is enabled */}
      {actions && !disableInternalDialog && (
        <DataTableDialog
          open={dialogs.dialogOpen}
          onOpenChange={dialogs.handleCloseDialog}
          selectedRow={dialogs.selectedRow}
          editingRow={dialogs.editingRow}
          isEditing={dialogs.isEditing}
          setIsEditing={dialogs.setIsEditing}
          setEditingRow={dialogs.setEditingRow}
          columnConfig={columnConfig}
          actions={actions}
          onSave={dialogs.handleSave}
          onDeleteClick={() => dialogs.setDeleteDialogOpen(true)}
          deleteDialogOpen={dialogs.deleteDialogOpen}
          setDeleteDialogOpen={dialogs.setDeleteDialogOpen}
          onConfirmDelete={dialogs.handleConfirmDelete}
          validationError={dialogs.validationError}
          isSaving={dialogs.isSaving}
        />
      )}
    </div>
  );
}
