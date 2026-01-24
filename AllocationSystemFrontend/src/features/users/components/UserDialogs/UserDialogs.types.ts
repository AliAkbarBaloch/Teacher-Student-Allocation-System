// UserDialogs.types.ts
import type { Role } from "@/features/roles/types/role.types";
import type { User, CreateUserRequest, UpdateUserRequest } from "../../types/user.types";

export interface UserDialogsProps {
    isCreateDialogOpen: boolean;
    setIsCreateDialogOpen: (open: boolean) => void;

    isEditDialogOpen: boolean;
    setIsEditDialogOpen: (open: boolean) => void;

    isViewDialogOpen: boolean;
    setIsViewDialogOpen: (open: boolean) => void;

    isStatusDialogOpen: boolean;
    setIsStatusDialogOpen: (open: boolean) => void;

    isDeleteDialogOpen: boolean;
    setIsDeleteDialogOpen: (open: boolean) => void;

    isResetPasswordDialogOpen: boolean;
    setIsResetPasswordDialogOpen: (open: boolean) => void;

    selectedUser: User | null;
    roles: Role[];
    formLoading: boolean;

    statusTarget: { user: User | null; nextState: boolean };
    deleteTarget: User | null;

    resetTarget: User | null;

    onCreateSubmit: (payload: CreateUserRequest) => Promise<void>;
    onUpdateSubmit: (payload: UpdateUserRequest) => Promise<void>;

    onStatusChange: () => Promise<void>;
    onDelete: () => Promise<void>;

    onOpenEdit: (user: User) => Promise<void>;
    onStatusTargetChange: (target: { user: User | null; nextState: boolean }) => void;

    onResetPassword: (user: User, newPassword: string) => Promise<void>;
    onResetTargetChange: (user: User | null) => void;

    isSubmitting: boolean;

    fieldErrors?: Partial<Record<string, string>>;
}
