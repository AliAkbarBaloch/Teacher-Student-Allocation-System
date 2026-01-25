import { useTranslation } from "react-i18next";
import { Loader2 } from "lucide-react";

import {
    Dialog,
    DialogBody,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";

import { UserForm } from "../UserForm";
import type { UserDialogsProps } from "./UserDialogs.types";

type Props = Pick<
    UserDialogsProps,
    | "isEditDialogOpen"
    | "setIsEditDialogOpen"
    | "formLoading"
    | "selectedUser"
    | "roles"
    | "onUpdateSubmit"
    | "isSubmitting"
    | "fieldErrors"
>;

export function EditUserDialog({
    isEditDialogOpen,
    setIsEditDialogOpen,
    formLoading,
    selectedUser,
    roles,
    onUpdateSubmit,
    isSubmitting,
    fieldErrors,
}: Props) {
    const { t } = useTranslation("users");

    return (
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
                                onSubmit={async (payload) => {
                                    try {
                                        await onUpdateSubmit(payload);
                                        setIsEditDialogOpen(false);
                                    } catch {
                                        //
                                    }
                                }}
                                onCancel={() => setIsEditDialogOpen(false)}
                                isLoading={isSubmitting}
                                fieldErrors={fieldErrors}
                            />
                        )
                    )}
                </DialogBody>
            </DialogContent>
        </Dialog>
    );
}
