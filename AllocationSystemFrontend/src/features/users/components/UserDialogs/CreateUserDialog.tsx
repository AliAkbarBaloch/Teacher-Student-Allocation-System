import { useTranslation } from "react-i18next";

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
    | "isCreateDialogOpen"
    | "setIsCreateDialogOpen"
    | "roles"
    | "onCreateSubmit"
    | "isSubmitting"
    | "fieldErrors"
>;

export function CreateUserDialog({
    isCreateDialogOpen,
    setIsCreateDialogOpen,
    roles,
    onCreateSubmit,
    isSubmitting,
    fieldErrors,
}: Props) {
    const { t } = useTranslation("users");

    return (
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
            <DialogContent className="max-w-3xl">
                <DialogHeader>
                    <DialogTitle>{t("form.title.create")}</DialogTitle>
                    <DialogDescription>{t("form.description")}</DialogDescription>
                </DialogHeader>

                <DialogBody>
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
    );
}
