import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { useTranslation } from "react-i18next";

import {
    Dialog,
    DialogBody,
    DialogContent,
    DialogDescription,
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

import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import type { Role } from "@/features/roles/types/role.types";
import type { User, CreateUserRequest, UpdateUserRequest } from "../types/user.types";
import { UserForm } from "./UserForm";
import { Button } from "@/components/ui/button";

//props interface. what we get from UserPage. inputs 
interface UserDialogsProps {
    // if create dialog is open 
    isCreateDialogOpen: boolean;
    setIsCreateDialogOpen: (open: boolean) => void;
    // if edit dialog is open 
    isEditDialogOpen: boolean;
    setIsEditDialogOpen: (open: boolean) => void;
    // if view dialog is open 
    isViewDialogOpen: boolean;
    setIsViewDialogOpen: (open: boolean) => void;
    //if status dialog is open 
    isStatusDialogOpen: boolean;
    setIsStatusDialogOpen: (open: boolean) => void;
    //if delete dialog is open 
    isDeleteDialogOpen: boolean;
    setIsDeleteDialogOpen: (open: boolean) => void;

    // if reset password dialog is open 
    isResetPasswordDialogOpen: boolean;
    setIsResetPasswordDialogOpen: (open: boolean) => void;

    // Data
    //is the user currently selected (view/edit)
    selectedUser: User | null;
    //list of roles for dropdown 
    roles: Role[];
    formLoading: boolean;

    //confirmation targets 
    statusTarget: { user: User | null; nextState: boolean };
    deleteTarget: User | null;

    // Password reset target
    resetTarget: User | null;

    // Handlers
    onCreateSubmit: (payload: CreateUserRequest) => Promise<void>;
    onUpdateSubmit: (payload: UpdateUserRequest) => Promise<void>;

    onStatusChange: () => Promise<void>;
    onDelete: () => Promise<void>;

    onOpenEdit: (user: User) => Promise<void>;

    onStatusTargetChange: (target: { user: User | null; nextState: boolean }) => void;

    // Reset password handlers
    onResetPassword: (user: User, newPassword: string) => Promise<void>;
    onResetTargetChange: (user: User | null) => void;

    // State
    isSubmitting: boolean;

    //form fileld errors (e.g., duplicate email)
    fieldErrors?: Partial<Record<string, string>>;

}

//Component 
export function UserDialogs({
    isCreateDialogOpen,
    setIsCreateDialogOpen,
    isEditDialogOpen,
    setIsEditDialogOpen,
    isViewDialogOpen,
    setIsViewDialogOpen,
    isStatusDialogOpen,
    setIsStatusDialogOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    isResetPasswordDialogOpen,
    setIsResetPasswordDialogOpen,
    selectedUser,
    roles,
    formLoading,
    statusTarget,
    resetTarget,
    onCreateSubmit,
    onUpdateSubmit,
    onStatusChange,
    onDelete,
    onOpenEdit,
    onStatusTargetChange,
    onResetPassword,
    onResetTargetChange,
    isSubmitting,
    fieldErrors,

}: UserDialogsProps) {

    const { t } = useTranslation("users");

    const { t: tCommon } = useTranslation("common");

    // Reset password local state
    
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [resetError, setResetError] = useState<string | null>(null);

    // Clear reset fields whenever the dialog closes
    useEffect(() => {
        if (!isResetPasswordDialogOpen) {
            setNewPassword("");
            setConfirmPassword("");
            setResetError(null);
        }
    }, [isResetPasswordDialogOpen]);

    const handleConfirmResetPassword = async () => {
        setResetError(null);

        // Frontend-only validation
        if (!newPassword || newPassword.length < 6) {
            setResetError(t("resetPassword.errors.minLength"));
            return;
        }
        if (newPassword !== confirmPassword) {
            setResetError(t("resetPassword.errors.mismatch"));
            return;
        }

        if (!resetTarget) {
            setResetError(t("resetPassword.errors.noUser"));
            return;
        }

        try {
            await onResetPassword(resetTarget, newPassword);
            setIsResetPasswordDialogOpen(false);
            onResetTargetChange(null);
        } catch {
            // toast handled by parent
        }
    };

    return (
        <>
            
            {/* Create Dialog */}

            <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
                <DialogContent className="max-w-3xl">
                    <DialogHeader>
                        <DialogTitle>{t("form.title.create")}</DialogTitle>
                        <DialogDescription>{t("form.description")}</DialogDescription>
                    </DialogHeader>

                    <DialogBody>
                        {/* using UserForm component*/}
                        <UserForm
                            user={null}
                            roles={roles}
                            onSubmit={async (payload) => {
                                try {
                                await onCreateSubmit(payload);
                                setIsCreateDialogOpen(false);
                            } catch {
                                //
                            }
                            }}
                            onCancel={() => setIsCreateDialogOpen(false)}
                            isLoading={isSubmitting}
                            fieldErrors={fieldErrors}
                        />
                    </DialogBody>
                </DialogContent>
            </Dialog>

            {/* Edit Dialog */}
        
            <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                <DialogContent className="max-w-3xl">
                    <DialogHeader>
                        <DialogTitle>{t("form.title.edit")}</DialogTitle>
                        <DialogDescription>{t("form.description")}</DialogDescription>
                    </DialogHeader>

                    <DialogBody>
                        {formLoading ? (
                            <div className="flex min-h-[200px] items-center justify-center">
                                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                            </div>
                        ) : (
                            selectedUser && (
                                <UserForm
                                    user={selectedUser}
                                    roles={roles}
                                    onSubmit={async (payload) => 
                                        {
                                            await onUpdateSubmit(payload);
                                        }
                                    }
                                    onCancel={() => setIsEditDialogOpen(false)}
                                    isLoading={isSubmitting}
                                    fieldErrors={fieldErrors}
                                />
                            )
                        )}
                    </DialogBody>
                </DialogContent>
            </Dialog>

            {/* View Dialog */}
            <ViewDialog
                open={isViewDialogOpen}
                onOpenChange={setIsViewDialogOpen}
                data={selectedUser}
                title={t("form.title.view")}
                description={t("form.description")}
                maxWidth="2xl"
                onEdit={() => {
                    setIsViewDialogOpen(false);
                    if (selectedUser) {
                        onOpenEdit(selectedUser);
                    }
                }}
                editLabel={tCommon("actions.edit")}
                closeLabel={tCommon("actions.close")}
                renderCustomContent={(user) => (
                    <DialogBody>
                        <div className="grid gap-4 text-sm">
                            <div>
                                <p className="text-muted-foreground">{t("fields.fullName")}</p>
                                <p className="font-medium">{user.fullName}</p>
                            </div>

                            <div>
                                <p className="text-muted-foreground">{t("fields.email")}</p>
                                <p className="font-medium">{user.email}</p>
                            </div>

                            <div>
                                <p className="text-muted-foreground">{t("fields.role")}</p>
                                <p className="font-medium">{user.role}</p>
                            </div>

                            <div>
                                <p className="text-muted-foreground">{t("fields.enabled")}</p>
                                <p className="font-medium">{user.enabled ? t("status.enabled") : t("status.disabled")}</p>
                            </div>

                            {user.phoneNumber && (
                                <div>
                                    <p className="text-muted-foreground">{t("fields.phoneNumber")}</p>
                                    <p className="font-medium">{user.phoneNumber}</p>
                                </div>
                            )}
                        </div>
                    </DialogBody>
                )}
            />

            {/* Activate / Deactivate Confirmation (AlertDialog) */}

            <AlertDialog
                open={isStatusDialogOpen}
                onOpenChange={(open) => {
                    setIsStatusDialogOpen(open);
                    //reset 
                    if (!open) {
                        onStatusTargetChange({ user: null, nextState: false });
                    }
                }}
            >
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>
                            {statusTarget.nextState
                                ? t("status.activateTitle")
                                : t("status.deactivateTitle")}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {statusTarget.nextState
                                ? t("status.activateDescription")
                                : t("status.deactivateDescription")}
                        </AlertDialogDescription>
                    </AlertDialogHeader>

                    <AlertDialogFooter>
                        <AlertDialogCancel disabled={isSubmitting}>
                            {tCommon("actions.cancel")}
                        </AlertDialogCancel>

                        <AlertDialogAction
                            onClick={onStatusChange}
                            disabled={isSubmitting || !statusTarget.user}
                            className={
                                statusTarget.nextState
                                    ? ""
                                    : "bg-destructive text-white hover:bg-destructive/90"
                            }
                        >
                            {isSubmitting ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : statusTarget.nextState ? (
                                t("actions.activate")
                            ) : (
                                t("actions.deactivate")
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* Delete Confirmation */}

            <DeleteConfirmationDialog
                open={isDeleteDialogOpen}
                onOpenChange={setIsDeleteDialogOpen}
                onConfirm={onDelete}
                title={t("delete.title")}
                description={t("delete.description")}
                cancelLabel={t("delete.cancel")}
                confirmLabel={t("delete.confirm")}
                isSubmitting={isSubmitting}
            />

            {/* Reset Password Dialog (Dialog) */}

            <Dialog
                open={isResetPasswordDialogOpen}
                onOpenChange={(open) => {
                    setIsResetPasswordDialogOpen(open);
                    if (!open) {
                        onResetTargetChange(null);
                    }
                }}
            >
                <DialogContent className="max-w-xl">
                    <DialogHeader>
                        <DialogTitle>{t("resetPassword.title")}</DialogTitle>
                        <DialogDescription>
                            {resetTarget
                                ? t("resetPassword.description", { email: resetTarget.email })
                                : t("resetPassword.descriptionNoUser")}
                        </DialogDescription>
                    </DialogHeader>

                    <DialogBody>
                        {resetError && (
                            <div className="mb-4 rounded-md border border-destructive/20 bg-destructive/10 p-3 text-sm text-destructive">
                                {resetError}
                            </div>
                        )}

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="newPassword">{t("resetPassword.fields.newPassword")}</Label>
                                <Input
                                    id="newPassword"
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    disabled={isSubmitting}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="confirmPassword">{t("resetPassword.fields.confirmPassword")}</Label>
                                <Input
                                    id="confirmPassword"
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    disabled={isSubmitting}
                                />
                            </div>

                            <p className="text-xs text-muted-foreground">
                                {t("resetPassword.hint")}
                            </p>

                            <div className="flex justify-end gap-2 pt-2">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={() => setIsResetPasswordDialogOpen(false)}
                                    disabled={isSubmitting}
                                >
                                    {tCommon("actions.cancel")}
                                </Button>

                                <Button
                                    type="button"
                                    onClick={handleConfirmResetPassword}
                                    disabled={isSubmitting || !resetTarget}
                                >
                                    {isSubmitting ? (
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    ) : null}
                                    {t("resetPassword.actions.confirm")}
                                </Button>
                            </div>
                        </div>
                    </DialogBody>
                </DialogContent>
            </Dialog>
        </>
    );
}
