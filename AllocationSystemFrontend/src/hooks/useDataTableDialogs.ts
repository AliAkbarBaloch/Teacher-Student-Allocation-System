import { useState, useCallback, useEffect } from "react";
import type { DataTableActions } from "@/types/datatable.types";

interface UseDataTableDialogsOptions<TData> {
  actions?: DataTableActions<TData>;
  disableInternalDialog?: boolean;
  validateOnUpdate?: (row: TData) => string | null | Promise<string | null>;
}

/**
 * Hook to manage DataTable dialog state and handlers
 */
export function useDataTableDialogs<TData>({
  actions,
  disableInternalDialog = false,
  validateOnUpdate,
}: UseDataTableDialogsOptions<TData>) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedRow, setSelectedRow] = useState<TData | null>(null);
  const [editingRow, setEditingRow] = useState<Partial<TData>>({});
  const [isEditing, setIsEditing] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Clear validation error when entering edit mode
  useEffect(() => {
    if (isEditing) {
      setValidationError(null);
    }
  }, [isEditing]);

  const handleViewRow = useCallback(
    (row: TData) => {
      if (disableInternalDialog) {
        actions?.onView?.(row);
        return;
      }
      setSelectedRow(row);
      setEditingRow({ ...row });
      setIsEditing(false);
      setDialogOpen(true);
      actions?.onView?.(row);
    },
    [actions, disableInternalDialog]
  );

  const handleEditRow = useCallback(
    (row: TData) => {
      if (disableInternalDialog) {
        actions?.onEdit?.(row);
        return;
      }
      setSelectedRow(row);
      setEditingRow({ ...row });
      setIsEditing(true);
      setValidationError(null);
      setDialogOpen(true);
      actions?.onEdit?.(row);
    },
    [actions, disableInternalDialog]
  );

  const handleDeleteClick = useCallback(
    (row: TData) => {
      if (disableInternalDialog) {
        actions?.onDelete?.(row);
        return;
      }
      setSelectedRow(row);
      setDeleteDialogOpen(true);
    },
    [actions, disableInternalDialog]
  );

  const handleConfirmDelete = useCallback(() => {
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

  const handleSave = useCallback(async () => {
    if (selectedRow && actions?.onUpdate) {
      setIsSaving(true);
      setValidationError(null);

      try {
        if (validateOnUpdate) {
          const validationResult = await Promise.resolve(validateOnUpdate(editingRow as TData));
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
        setValidationError(
          err instanceof Error ? err.message : "An error occurred while saving"
        );
      } finally {
        setIsSaving(false);
      }
    }
  }, [selectedRow, editingRow, actions, validateOnUpdate]);

  const handleCloseDialog = useCallback(
    (open: boolean) => {
      if (!open) {
        if (!deleteDialogOpen) {
          setDialogOpen(false);
          setIsEditing(false);
          setSelectedRow(null);
          setEditingRow({});
          setValidationError(null);
        } else {
          setDialogOpen(false);
        }
      }
    },
    [deleteDialogOpen]
  );

  return {
    dialogOpen,
    setDialogOpen,
    deleteDialogOpen,
    setDeleteDialogOpen,
    selectedRow,
    setSelectedRow,
    editingRow,
    setEditingRow,
    isEditing,
    setIsEditing,
    validationError,
    setValidationError,
    isSaving,
    handleViewRow,
    handleEditRow,
    handleDeleteClick,
    handleConfirmDelete,
    handleSave,
    handleCloseDialog,
  };
}

