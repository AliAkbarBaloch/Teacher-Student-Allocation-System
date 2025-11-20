import { useState, useCallback } from "react";

/**
 * Generic dialog state management hook
 * Consolidates multiple dialog states into a single discriminated union
 */
export type DialogState<T> =
  | { type: "create" }
  | { type: "edit"; item: T }
  | { type: "view"; item: T }
  | { type: "status"; item: T; nextState: boolean }
  | { type: "delete"; item: T }
  | { type: null };

export function useDialogState<T>() {
  const [dialogState, setDialogState] = useState<DialogState<T>>({ type: null });

  const openCreate = useCallback(() => {
    setDialogState({ type: "create" });
  }, []);

  const openEdit = useCallback((item: T) => {
    setDialogState({ type: "edit", item });
  }, []);

  const openView = useCallback((item: T) => {
    setDialogState({ type: "view", item });
  }, []);

  const openStatus = useCallback((item: T, nextState: boolean) => {
    setDialogState({ type: "status", item, nextState });
  }, []);

  const openDelete = useCallback((item: T) => {
    setDialogState({ type: "delete", item });
  }, []);

  const closeDialog = useCallback(() => {
    setDialogState({ type: null });
  }, []);

  return {
    dialogState,
    openCreate,
    openEdit,
    openView,
    openStatus,
    openDelete,
    closeDialog,
    isOpen: dialogState.type !== null,
  };
}

