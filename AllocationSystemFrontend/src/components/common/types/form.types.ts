import React from "react";

/**
 * Supported field types for form inputs
 */
export type FieldType =
  | "text"
  | "number"
  | "email"
  | "password"
  | "textarea"
  | "select"
  | "checkbox"
  | "datetime-local"
  | "date"
  | "time";

/**
 * Validation rules for form fields
 */
export interface ValidationRules {
  required?: boolean;
  min?: number;
  max?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  custom?: (value: unknown, formData: unknown) => string | null;
}

/**
 * Select option for select fields
 */
export interface SelectOption {
  value: string | number;
  label: string;
}

/**
 * Props passed to custom render functions
 */
export interface FieldRenderProps<TData = unknown> {
  value: unknown;
  onChange: (value: unknown) => void;
  error?: string;
  disabled?: boolean;
  field: FieldConfig<TData>;
  formData: TData;
}

/**
 * Field configuration for GenericForm
 * This configuration is used for both form rendering and view rendering
 */
export interface FieldConfig<TData = unknown> {
  /** Field name (key in the data object) */
  name: keyof TData;
  
  /** Field type */
  type: FieldType;
  
  /** Label for the field */
  label: string;
  
  /** Placeholder text */
  placeholder?: string;
  
  /** Whether field is required (for form mode) */
  required?: boolean;
  
  /** Whether field is disabled (can be a function for dynamic disabling) */
  disabled?: boolean | ((data: TData) => boolean);
  
  /** Validation rules */
  validation?: ValidationRules;
  
  /** Options for select fields */
  options?: SelectOption[] | (() => Promise<SelectOption[]>);
  
  /** Custom render function (overrides default rendering) */
  render?: (props: FieldRenderProps<TData>) => React.ReactNode;
  
  /** Transform functions for input/output */
  transform?: {
    /** Transform value for display in input */
    input?: (value: unknown) => unknown;
    /** Transform value before submission */
    output?: (value: unknown) => unknown;
  };
  
  /** Grid column span (1 or 2 columns) */
  colSpan?: 1 | 2;
  
  /** Maximum length for text fields */
  maxLength?: number;
  
  /** Minimum value for number fields */
  min?: number;
  
  /** Maximum value for number fields */
  max?: number;
  
  /** Step value for number fields */
  step?: number;
  
  /** Rows for textarea fields */
  rows?: number;
  
  /** Custom format function for view mode (read-only display) */
  viewFormat?: (value: unknown, data: TData) => React.ReactNode | string;
  
  /** Custom label for view mode (can differ from form label) */
  viewLabel?: string;
  
  /** Description text shown below the field */
  description?: string;
  
  /** Whether to show this field in view mode */
  showInView?: boolean;
  
  /** Whether to show this field in form mode */
  showInForm?: boolean;
}

/**
 * Props for GenericFormDialog component
 */
export interface GenericFormDialogProps {
  /** Whether dialog is open */
  open: boolean;
  
  /** Callback when open state changes */
  onOpenChange: (open: boolean) => void;
  
  /** Dialog title */
  title: string;
  
  /** Dialog description */
  description?: string;
  
  /** Maximum width of the dialog */
  maxWidth?: "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl" | "full";
  
  /** Form content (typically GenericForm component) */
  children: React.ReactNode;
  
  /** Additional CSS classes for the dialog content */
  className?: string;
}

/**
 * Props for GenericForm component
 */
export interface GenericFormProps<TData, TCreateRequest, TUpdateRequest> {
  /** Field configurations */
  fields: FieldConfig<TData>[];
  
  /** Initial data (for edit mode) */
  initialData?: TData | null;
  
  /** Submit handler */
  onSubmit: (data: TCreateRequest | TUpdateRequest) => Promise<void>;
  
  /** Cancel handler */
  onCancel: () => void;
  
  /** Whether form is loading */
  isLoading?: boolean;
  
  /** External error message */
  error?: string | null;
  
  /** Form mode: create or edit */
  mode?: "create" | "edit";
  
  /** Translation namespace for i18n */
  translationNamespace?: string;
  
  /** Additional CSS classes for the form element */
  className?: string;
}

/**
 * Form state for internal use
 */
export interface FormState<TData> {
  data: Partial<TData>;
  errors: Partial<Record<keyof TData, string>>;
  isSubmitting: boolean;
}
