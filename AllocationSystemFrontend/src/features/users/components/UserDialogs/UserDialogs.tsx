// UserDialogs.tsx
import { useTranslation } from "react-i18next";

import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";

import type { UserDialogsProps } from "./UserDialogs.types";
import { CreateUserDialog } from "./CreateUserDialog";
import { EditUserDialog } from "./EditUserDialog";
import { ViewUserDialog } from "./ViewUserDialog";
import { StatusConfirmationDialog } from "./StatusConfirmationDialog";
import { ResetPasswordDialog } from "./ResetPasswordDialog";

export function UserDialogs(props: UserDialogsProps) {
    const { t } = useTranslation("users");

    return (
        <>
            <CreateUserDialog {...props} />
            <EditUserDialog {...props} />
            <ViewUserDialog {...props} />
            <StatusConfirmationDialog {...props} />

            <DeleteConfirmationDialog
                open={props.isDeleteDialogOpen}
                onOpenChange={props.setIsDeleteDialogOpen}
                onConfirm={props.onDelete}
                title={t("delete.title")}
                description={t("delete.description")}
                cancelLabel={t("delete.cancel")}
                confirmLabel={t("delete.confirm")}
                isSubmitting={props.isSubmitting}
            />

            <ResetPasswordDialog {...props} />
        </>
    );
}

