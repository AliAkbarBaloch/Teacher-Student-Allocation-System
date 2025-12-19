import { useCallback, useState } from "react";
import { useDialogState } from "@/hooks/useDialogState";
import type { CreditHourTracking, UpdateCreditHourTrackingRequest } from "../types/creditHourTracking.types";

/**
 * Hook for managing credit hour tracking dialogs and their actions
 * Encapsulates dialog state and action handlers
 */
export function useCreditHourTrackingDialogs(
  onUpdate: (id: number, data: UpdateCreditHourTrackingRequest) => Promise<void>,
  onDelete: (id: number) => Promise<void>
) {
  const dialogs = useDialogState();
  const [selectedEntry, setSelectedEntry] = useState<CreditHourTracking | null>(null);
  const [entryToDelete, setEntryToDelete] = useState<CreditHourTracking | null>(null);

  const handleOpenView = useCallback((entry: CreditHourTracking) => {
    setSelectedEntry(entry);
    dialogs.view.setIsOpen(true);
  }, [dialogs.view]);

  const handleEditClick = useCallback((entry: CreditHourTracking) => {
    setSelectedEntry(entry);
    dialogs.edit.setIsOpen(true);
  }, [dialogs.edit]);

  const handleDeleteClick = useCallback((entry: CreditHourTracking) => {
    setEntryToDelete(entry);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleUpdateSubmit = useCallback(
    async (id: number, data: UpdateCreditHourTrackingRequest) => {
      try {
        await onUpdate(id, data);
        dialogs.edit.setIsOpen(false);
        setSelectedEntry(null);
      } catch {
        // Error already handled in hook
      }
    },
    [onUpdate, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!entryToDelete) return;
    try {
      await onDelete(entryToDelete.id);
      dialogs.delete.setIsOpen(false);
      setEntryToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [entryToDelete, onDelete, dialogs.delete]);

  const closeViewDialog = useCallback(() => {
    dialogs.view.setIsOpen(false);
    setSelectedEntry(null);
  }, [dialogs.view]);

  return {
    dialogs: {
      view: {
        isOpen: dialogs.view.isOpen,
        setIsOpen: dialogs.view.setIsOpen,
        close: closeViewDialog,
      },
      edit: {
        isOpen: dialogs.edit.isOpen,
        setIsOpen: dialogs.edit.setIsOpen,
      },
      delete: {
        isOpen: dialogs.delete.isOpen,
        setIsOpen: dialogs.delete.setIsOpen,
      },
    },
    selectedEntry,
    setSelectedEntry,
    entryToDelete,
    handleOpenView,
    handleEditClick,
    handleDeleteClick,
    handleUpdateSubmit,
    handleDelete,
  };
}

export type UseCreditHourTrackingDialogsReturn = ReturnType<typeof useCreditHourTrackingDialogs>;
