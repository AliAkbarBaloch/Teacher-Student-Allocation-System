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
import type { ColumnConfig, DataTableActions } from "@/types/datatable.types";
import { DataTableField } from "./DataTableField";
import { DataTableDeleteDialog } from "./DataTableDeleteDialog";

/**
 * Props for the DataTableDialog component.
 */
interface DataTableDialogProps<TData> {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedRow: TData | null;
  editingRow: Partial<TData>;
  isEditing: boolean;
  setIsEditing: (editing: boolean) => void;
  setEditingRow: React.Dispatch<React.SetStateAction<Partial<TData>>>;
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

/**
 * A dialog component for viewing or editing a row in the DataTable.
 * Also manages a separate confirmation dialog for deletions.
 */
export function DataTableDialog<TData>({
  open,
  onOpenChange,
  selectedRow,
  editingRow,
  isEditing,
  setIsEditing,
  setEditingRow,
  columnConfig,
  actions,
  onSave,
  onDeleteClick,
  deleteDialogOpen,
  setDeleteDialogOpen,
  onConfirmDelete,
  validationError,
  isSaving = false,
}: DataTableDialogProps<TData>) {
  // Prevent main dialog from closing when delete dialog is open
  const handleMainDialogChange = React.useCallback((isOpen: boolean) => {
    if (!isOpen && deleteDialogOpen) {
      // Don't close if delete dialog is open - let it handle the close
      return;
    }
    onOpenChange(isOpen);
  }, [deleteDialogOpen, onOpenChange]);

  if (!actions || !selectedRow) {
    return null;
  }

  const renderFieldContainer = (field: string, label: string, config?: ColumnConfig, required?: boolean) => (
    <div key={field} className="grid gap-2">
      <label className="text-sm font-medium">
        {label}
        {required && <span className="text-destructive ml-1">*</span>}
      </label>
      <DataTableField
        field={field}
        value={editingRow[field as keyof TData]}
        config={config}
        isEditing={isEditing}
        editingRow={editingRow}
        setEditingRow={setEditingRow}
      />
    </div>
  );

  return (
    <>
      <Dialog open={open} onOpenChange={handleMainDialogChange}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{isEditing ? "Edit Record" : "View Record"}</DialogTitle>
            <DialogDescription>
              {isEditing ? "Update the information below and click Save." : "View the record details below."}
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
              ? columnConfig.map((config) =>
                renderFieldContainer(config.field, config.title, config, config.fieldRequired && isEditing)
              )
              : Object.keys(selectedRow).map((key) =>
                renderFieldContainer(key, key.replace(/([A-Z])/g, " $1").trim())
              )}
          </div>

          <DialogFooter className="flex-col-reverse sm:flex-row sm:justify-end gap-2">
            {isEditing ? (
              <>
                <Button variant="outline" onClick={() => setIsEditing(false)} className="w-full sm:w-auto">
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
                  <Button variant="destructive" className="text-white w-full sm:w-auto" onClick={() => onDeleteClick()}>
                    <Trash2 className="mr-2 h-4 w-4" />
                    Delete
                  </Button>
                )}
                {actions.onEdit && (
                  <Button onClick={() => setIsEditing(true)} className="w-full sm:w-auto">
                    <Pencil className="mr-2 h-4 w-4" />
                    Edit
                  </Button>
                )}
              </>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <DataTableDeleteDialog
        open={deleteDialogOpen}
        onOpenChange={(isOpen) => {
          setDeleteDialogOpen(isOpen);
          if (!isOpen && open) onOpenChange(false);
        }}
        onConfirm={onConfirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
      />
    </>
  );
}
