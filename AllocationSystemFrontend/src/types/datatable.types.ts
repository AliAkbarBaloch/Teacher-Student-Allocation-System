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
  format?: "currency" | "number" | "date" | "capitalize" | "lowercase" | "uppercase";
  currencyCode?: string;
  enableSorting?: boolean;
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
}

