/**
 * Common state properties for a single dialog
 */
export interface BaseDialogState {
    isOpen: boolean;
    setIsOpen: (open: boolean) => void;
}

/**
 * Standard dialog trigger states for CRUD operations
 */
export interface CrudDialogState {
    isCreateDialogOpen: boolean;
    setIsCreateDialogOpen: (open: boolean) => void;
    isEditDialogOpen: boolean;
    setIsEditDialogOpen: (open: boolean) => void;
    isViewDialogOpen: boolean;
    setIsViewDialogOpen: (open: boolean) => void;
    isDeleteDialogOpen: boolean;
    setIsDeleteDialogOpen: (open: boolean) => void;
}

/**
 * Common data and handlers for CRUD dialogs
 */
export interface CrudDialogHandlers<T, CreatePayload, UpdatePayload> {
    selectedItem: T | null;
    onSelectedChange: (item: T | null) => void;
    onCreateSubmit: (data: CreatePayload) => Promise<void>;
    onUpdateSubmit: (data: UpdatePayload) => Promise<void>;
    onDelete: () => void | Promise<void>;
    isSubmitting: boolean;
}
