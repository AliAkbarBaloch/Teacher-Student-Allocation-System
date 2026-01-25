import { useTranslation } from "react-i18next";

import { ViewDialog } from "@/components/common/ViewDialog";
import { DialogBody } from "@/components/ui/dialog";

import type { UserDialogsProps } from "./UserDialogs.types";

type Props = Pick<
    UserDialogsProps,
    "isViewDialogOpen" | "setIsViewDialogOpen" | "selectedUser" | "onOpenEdit"
>;

export function ViewUserDialog({
    isViewDialogOpen,
    setIsViewDialogOpen,
    selectedUser,
    onOpenEdit,
}: Props) {
    const { t } = useTranslation("users");
    const { t: tCommon } = useTranslation("common");

    return (
        <ViewDialog
            open={isViewDialogOpen}
            onOpenChange={setIsViewDialogOpen}
            data={selectedUser}
            title={t("form.title.view")}
            description={t("form.description")}
            maxWidth="2xl"
            onEdit={() => {
                setIsViewDialogOpen(false);
                if (selectedUser) onOpenEdit(selectedUser);
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
                            <p className="font-medium">
                                {user.enabled ? t("status.enabled") : t("status.disabled")}
                            </p>
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
    );
}
