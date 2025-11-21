import { useState } from "react";

export interface DialogState {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
}

export interface DialogStates {
  create: DialogState;
  edit: DialogState;
  view: DialogState;
  delete: DialogState;
}

/**
 * Custom hook to manage common CRUD dialog states.
 * Reduces boilerplate by centralizing dialog state management.
 * 
 * @returns Object containing dialog states for create, edit, view, and delete operations
 * 
 * @example
 * ```tsx
 * const dialogs = useDialogState();
 * 
 * // Open create dialog
 * dialogs.create.setIsOpen(true);
 * 
 * // Check if edit dialog is open
 * if (dialogs.edit.isOpen) { ... }
 * ```
 */
export function useDialogState(): DialogStates {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);

  return {
    create: { isOpen: isCreateOpen, setIsOpen: setIsCreateOpen },
    edit: { isOpen: isEditOpen, setIsOpen: setIsEditOpen },
    view: { isOpen: isViewOpen, setIsOpen: setIsViewOpen },
    delete: { isOpen: isDeleteOpen, setIsOpen: setIsDeleteOpen },
  };
}
