import React, { useCallback, useMemo } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { ColumnConfig } from "@/types/datatable.types";
import type { FieldConfig } from "./types/form.types";

export interface ViewDialogProps<TData> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  data: TData | null;
  columnConfig?: ColumnConfig[];
  /** Field configuration for auto-rendering (reuses same config as GenericForm) */
  fieldConfig?: FieldConfig<TData>[];
  title?: string;
  description?: string;
  onEdit?: () => void;
  editLabel?: string;
  closeLabel?: string;
  maxWidth?: "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl" | "full";
  renderCustomContent?: (data: TData) => React.ReactNode;
}

const maxWidthClasses = {
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-xl",
  "2xl": "max-w-2xl",
  "3xl": "max-w-3xl",
  "4xl": "max-w-4xl",
  "5xl": "max-w-5xl",
  full: "max-w-full",
};

export function ViewDialog<TData>({
  open,
  onOpenChange,
  data,
  columnConfig,
  fieldConfig,
  title = "View Details",
  description,
  onEdit,
  editLabel = "Edit",
  closeLabel = "Close",
  maxWidth = "2xl",
  renderCustomContent,
}: ViewDialogProps<TData>) {
  const renderFieldValue = useCallback((_field: string, value: unknown, config?: ColumnConfig) => {
    // If there's a format function, use it
    if (config?.format && typeof config.format === "function") {
      const formatted = config.format(value, data);
      // If format returns ReactNode, return it directly
      if (React.isValidElement(formatted) || typeof formatted === "object") {
        return formatted;
      }
      // Otherwise convert to string
      return String(formatted ?? "");
    }

    // Default rendering based on value type
    if (value === null || value === undefined) {
      return <span className="text-muted-foreground">—</span>;
    }

    if (typeof value === "boolean") {
      return value ? "Yes" : "No";
    }

    if (value instanceof Date) {
      return value.toLocaleDateString();
    }

    return String(value);
  }, [data]);

  // Filter fields that should be shown in view mode
  const visibleFields = useMemo(
    () => fieldConfig?.filter((field) => field.showInView !== false) || [],
    [fieldConfig]
  );

  // Render field from fieldConfig - memoized
  const renderFieldFromConfig = useCallback((field: FieldConfig<TData>) => {
    // Type assertion: data is guaranteed to be non-null when this function is called
    // because we return early if data is null
    if (!data) return null;
    
    const value = (data as Record<string, unknown>)[String(field.name)];
    
    // Skip fields that shouldn't be shown in view mode
    if (field.showInView === false) {
      return null;
    }

    // Use custom viewFormat if provided
    if (field.viewFormat) {
      const formatted = field.viewFormat(value, data as TData);
      return (
        <div
          key={String(field.name)}
          className={`grid gap-1 ${field.colSpan === 2 ? "md:col-span-2" : ""}`}
        >
          <label className="text-sm font-medium">
            {field.viewLabel || field.label}
          </label>
          <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
            {React.isValidElement(formatted) ? formatted : String(formatted ?? "—")}
          </div>
        </div>
      );
    }

    // Default formatting based on field type
    let displayValue: React.ReactNode = "—";
    
    if (value === null || value === undefined || value === "") {
      displayValue = <span className="text-muted-foreground">—</span>;
    } else if (typeof value === "boolean") {
      displayValue = value ? "Yes" : "No";
    } else if (typeof value === "string" && field.type === "datetime-local") {
      // Try to parse as date
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        displayValue = date.toLocaleString();
      } else {
        displayValue = value;
      }
    } else if (field.type === "select" && field.options) {
      // Try to find the label for the selected value
      const options = Array.isArray(field.options) ? field.options : [];
      const option = options.find((opt) => String(opt.value) === String(value));
      displayValue = option ? option.label : String(value);
    } else {
      displayValue = String(value);
    }

    return (
      <div
        key={String(field.name)}
        className={`grid gap-1 ${field.colSpan === 2 ? "md:col-span-2" : ""}`}
      >
        <label className="text-sm font-medium">
          {field.viewLabel || field.label}
        </label>
        <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
          {displayValue}
        </div>
      </div>
    );
  }, [data]);

  if (!data) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className={`${maxWidthClasses[maxWidth]} max-h-[90vh] overflow-y-auto`}>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>

        {renderCustomContent ? (
          renderCustomContent(data)
        ) : (
          <div className="py-2 px-4">
            {visibleFields.length > 0 ? (
              // Use fieldConfig to render fields (new approach - reuses form config)
              <div className="grid gap-4 md:grid-cols-2">
                {visibleFields.map((field) => renderFieldFromConfig(field))}
              </div>
            ) : columnConfig && columnConfig.length > 0 ? (
              // Use columnConfig to render fields (existing approach)
              <div className="grid gap-4">
                {columnConfig.map((config) => {
                  const value = (data as Record<string, unknown>)[config.field];
                  return (
                    <div key={config.field} className="grid gap-2">
                      <p className="text-sm font-medium text-muted-foreground">{config.title}</p>
                      <div className="text-base">
                        {renderFieldValue(config.field, value, config)}
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              // Fallback: render all fields from data object
              <div className="grid gap-4">
                {Object.entries(data as Record<string, unknown>).map(([key, value]) => (
                  <div key={key} className="grid gap-1">
                    <p className="text-sm font-medium text-muted-foreground capitalize">
                      {key.replace(/([A-Z])/g, " $1").trim()}
                    </p>
                    <div className="text-base">
                      {renderFieldValue(key, value)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        <DialogFooter className="flex-col-reverse sm:flex-row sm:justify-end gap-2 pb-4 px-4">
          <Button variant="outline" onClick={() => onOpenChange(false)} className="w-full sm:w-auto">
            {closeLabel}
          </Button>
          {onEdit && (
            <Button onClick={onEdit} className="w-full sm:w-auto">
              {editLabel}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

