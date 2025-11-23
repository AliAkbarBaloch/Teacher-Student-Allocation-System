/**
 * DataTable type definitions
 * 
 * Types for the reusable DataTable component
 */

import type { ColumnDef } from "@tanstack/react-table";

/**
 * Configuration for table actions (view, edit, delete, update)
 */
export interface DataTableActions<TData> {
  onView?: (row: TData) => void;
  onEdit?: (row: TData) => void;
  onDelete?: (row: TData) => void;
  onUpdate?: (row: TData) => void | Promise<void>;
  customActions?: Array<{
    label: string | ((row: TData) => string);
    icon?: React.ReactNode | ((row: TData) => React.ReactNode);
    onClick: (row: TData) => void;
    className?: string;
    separator?: boolean;
  }>;
  labels?: {
    view?: string;
    edit?: string;
    delete?: string;
  };
}

/**
 * Field types for form inputs in the edit dialog
 */
export type FieldType = "text" | "textarea" | "number" | "date" | "select" | "email" | "url";

/**
 * Select option for select field type
 */
export interface SelectOption {
  label: string;
  value: string | number;
}

/**
 * Column configuration for column setup
 */
export interface ColumnConfig {
  field: string;
  title: string;
  align?: "left" | "center" | "right";
  format?: "currency" | "number" | "date" | "capitalize" | "lowercase" | "uppercase" | ((value: unknown, row?: unknown) => string | React.ReactNode);
  currencyCode?: string;
  enableSorting?: boolean;
  width?: string | number; // Fixed width for column
  maxWidth?: string | number; // Maximum width for column
  // Form field configuration
  fieldType?: FieldType;
  fieldOptions?: SelectOption[]; // For select type
  fieldPlaceholder?: string;
  fieldRequired?: boolean;
  fieldDisabled?: boolean;
  fieldReadOnly?: boolean;
}

/**
 * Props for the DataTable component
 */
export interface DataTableProps<TData = Record<string, unknown>, TValue = unknown> {
  columns?: ColumnDef<TData, TValue>[];
  columnConfig?: ColumnConfig[];
  data: TData[];
  searchKey?: string;
  searchPlaceholder?: string;
  enableSearch?: boolean;
  enableColumnVisibility?: boolean;
  enablePagination?: boolean;
  enableRowSelection?: boolean;
  enableRowClick?: boolean;
  actions?: DataTableActions<TData>;
  actionsHeader?: string;
  // Loading and error states
  loading?: boolean;
  error?: string | null;
  emptyMessage?: string;
  // Pagination customization
  pageSizeOptions?: number[];
  defaultPageSize?: number;
  // Validation
  validateOnUpdate?: (row: TData) => string | null | Promise<string | null>;
  // Disable internal dialog management - parent component will handle dialogs
  disableInternalDialog?: boolean;
  // Table layout: "auto" (default) or "fixed" for equal column widths
  tableLayout?: "auto" | "fixed";
}

