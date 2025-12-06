import React from "react";
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

export interface ViewDialogProps<TData> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  data: TData | null;
  columnConfig?: ColumnConfig[];
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
  title = "View Details",
  description,
  onEdit,
  editLabel = "Edit",
  closeLabel = "Close",
  maxWidth = "2xl",
  renderCustomContent,
}: ViewDialogProps<TData>) {
  if (!data) return null;

  const renderFieldValue = (_field: string, value: unknown, config?: ColumnConfig) => {
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
  };

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
          <div className="grid gap-4 py-4">
            {columnConfig && columnConfig.length > 0 ? (
              // Use columnConfig to render fields
              columnConfig.map((config) => {
                const value = (data as Record<string, unknown>)[config.field];
                return (
                  <div key={config.field} className="grid gap-1">
                    <p className="text-sm font-medium text-muted-foreground">{config.title}</p>
                    <div className="text-base">
                      {renderFieldValue(config.field, value, config)}
                    </div>
                  </div>
                );
              })
            ) : (
              // Fallback: render all fields from data object
              Object.entries(data as Record<string, unknown>).map(([key, value]) => (
                <div key={key} className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground capitalize">
                    {key.replace(/([A-Z])/g, " $1").trim()}
                  </p>
                  <div className="text-base">
                    {renderFieldValue(key, value)}
                  </div>
                </div>
              ))
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

