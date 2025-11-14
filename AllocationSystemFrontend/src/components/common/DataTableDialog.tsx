import React from "react";
import { Pencil, Trash2, Save, AlertCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { ColumnDef } from "@tanstack/react-table";
import type { ColumnConfig, DataTableActions, FieldType } from "@/types/datatable.types";

interface DataTableDialogProps<TData, TValue> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedRow: TData | null;
  editingRow: Partial<TData>;
  isEditing: boolean;
  setIsEditing: (editing: boolean) => void;
  setEditingRow: React.Dispatch<React.SetStateAction<Partial<TData>>>;
  columns?: ColumnDef<TData, TValue>[];
  columnConfig?: ColumnConfig[];
  actions?: DataTableActions<TData>;
  onSave: () => Promise<void>;
  onDeleteClick: () => void;
  deleteDialogOpen: boolean;
  setDeleteDialogOpen: (open: boolean) => void;
  onConfirmDelete: () => void;
  validationError?: string | null;
  isSaving?: boolean;
}

export function DataTableDialog<TData, TValue>({
  open,
  onOpenChange,
  selectedRow,
  editingRow,
  isEditing,
  setIsEditing,
  setEditingRow,
  columns,
  columnConfig,
  actions,
  onSave,
  onDeleteClick,
  deleteDialogOpen,
  setDeleteDialogOpen,
  onConfirmDelete,
  validationError,
  isSaving = false,
}: DataTableDialogProps<TData, TValue>) {
  const renderField = (
    field: string,
    value: unknown,
    config?: ColumnConfig,
    isEditingField: boolean = false
  ) => {
    if (!isEditingField) {
      return (
        <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
          {String(value ?? "")}
        </div>
      );
    }

    const fieldType: FieldType = config?.fieldType || "text";
    const isDisabled = config?.fieldDisabled || config?.fieldReadOnly || field === "id";
    const placeholder = config?.fieldPlaceholder || "";

    switch (fieldType) {
      case "textarea":
        return (
          <textarea
            value={String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value,
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
            className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            rows={4}
          />
        );

      case "select":
        if (!config?.fieldOptions) {
          return (
            <Input
              value={String(value ?? "")}
              onChange={(e) =>
                setEditingRow({
                  ...editingRow,
                  [field]: e.target.value,
                })
              }
              disabled={isDisabled}
              placeholder={placeholder}
            />
          );
        }
        return (
          <Select
            value={String(value ?? "")}
            onValueChange={(val) =>
              setEditingRow({
                ...editingRow,
                [field]: val,
              })
            }
            disabled={isDisabled}
          >
            <SelectTrigger>
              <SelectValue placeholder={placeholder || "Select an option"} />
            </SelectTrigger>
            <SelectContent>
              {config.fieldOptions.map((option) => (
                <SelectItem key={String(option.value)} value={String(option.value)}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        );

      case "number":
        return (
          <Input
            type="number"
            value={String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value === "" ? "" : Number(e.target.value),
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
          />
        );

      case "date":
        return (
          <Input
            type="date"
            value={value instanceof Date ? value.toISOString().split("T")[0] : String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value,
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
          />
        );

      case "email":
        return (
          <Input
            type="email"
            value={String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value,
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
          />
        );

      case "url":
        return (
          <Input
            type="url"
            value={String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value,
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
          />
        );

      default:
        return (
          <Input
            value={String(value ?? "")}
            onChange={(e) =>
              setEditingRow({
                ...editingRow,
                [field]: e.target.value,
              })
            }
            disabled={isDisabled}
            placeholder={placeholder}
          />
        );
    }
  };

  if (!actions || !selectedRow) return null;

  // Prevent main dialog from closing when delete dialog is open
  const handleMainDialogChange = React.useCallback((isOpen: boolean) => {
    if (!isOpen && deleteDialogOpen) {
      // Don't close if delete dialog is open - let it handle the close
      return;
    }
    onOpenChange(isOpen);
  }, [deleteDialogOpen, onOpenChange]);

  return (
    <>
      <Dialog open={open} onOpenChange={handleMainDialogChange}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {isEditing ? "Edit Record" : "View Record"}
            </DialogTitle>
            <DialogDescription>
              {isEditing
                ? "Update the information below and click Save."
                : "View the record details below."}
            </DialogDescription>
          </DialogHeader>
          {validationError && (
            <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
              <AlertCircle className="h-4 w-4" />
              <span>{validationError}</span>
            </div>
          )}
          <div className="grid gap-4 py-4">
            {columnConfig
              ? columnConfig.map((config) => {
                  const value = editingRow[config.field as keyof TData];
                  return (
                    <div key={config.field} className="grid gap-2">
                      <label className="text-sm font-medium">
                        {config.title}
                        {config.fieldRequired && isEditing && (
                          <span className="text-destructive ml-1">*</span>
                        )}
                      </label>
                      {renderField(config.field, value, config, isEditing)}
                    </div>
                  );
                })
              : // For custom columns, show all fields from selectedRow
                Object.keys(selectedRow).map((key) => {
                  const value = editingRow[key as keyof TData];
                  return (
                    <div key={key} className="grid gap-2">
                      <label className="text-sm font-medium capitalize">
                        {key.replace(/([A-Z])/g, " $1").trim()}
                      </label>
                      {renderField(key, value, undefined, isEditing)}
                    </div>
                  );
                })}
          </div>
          <DialogFooter className="flex-col-reverse sm:flex-row sm:justify-end gap-2">
            {isEditing ? (
              <>
                <Button
                  variant="outline"
                  onClick={() => setIsEditing(false)}
                  className="w-full sm:w-auto"
                >
                  Cancel
                </Button>
                {actions.onUpdate && (
                  <Button onClick={onSave} className="w-full sm:w-auto" disabled={!!validationError || isSaving}>
                    {isSaving ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="mr-2 h-4 w-4" />
                        Save Changes
                      </>
                    )}
                  </Button>
                )}
              </>
            ) : (
              <>
                {actions.onDelete && (
                  <Button
                    variant="destructive"
                    className="text-white w-full sm:w-auto cursor-pointer"
                    onClick={(e) => {
                      e.stopPropagation();
                      // Open delete dialog - it will appear on top due to higher z-index
                      // The main dialog will stay open but be behind the delete dialog
                      onDeleteClick();
                    }}
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    Delete
                  </Button>
                )}
                {actions.onEdit && (
                  <Button
                    onClick={() => setIsEditing(true)}
                    className="w-full sm:w-auto cursor-pointer"
                  >
                    <Pencil className="mr-2 h-4 w-4" />
                    Edit
                  </Button>
                )}
              </>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog 
        open={deleteDialogOpen} 
        onOpenChange={(isOpen) => {
          setDeleteDialogOpen(isOpen);
          // When delete dialog closes, also close the main dialog if it was open
          if (!isOpen && open) {
            onOpenChange(false);
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete this record.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeleteDialogOpen(false)}>
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction asChild>
              <Button variant="destructive" onClick={onConfirmDelete}>
                Delete
              </Button>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

