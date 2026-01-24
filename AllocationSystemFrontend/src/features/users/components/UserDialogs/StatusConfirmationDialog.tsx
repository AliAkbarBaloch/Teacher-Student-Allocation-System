import { useTranslation } from "react-i18next";
import { Loader2 } from "lucide-react";

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

import type { UserDialogsProps } from "./UserDialogs.types";

type Props = Pick<
    UserDialogsProps,
    | "isStatusDialogOpen"
    | "setIsStatusDialogOpen"
    | "statusTarget"
    | "onStatusTargetChange"
    | "onStatusChange"
    | "isSubmitting"
>;

export function StatusConfirmationDialog({
    isStatusDialogOpen,
    setIsStatusDialogOpen,
    statusTarget,
    onStatusTargetChange,
    onStatusChange,
    isSubmitting,
}: Props) {
    const { t } = useTranslation("users");
    const { t: tCommon } = useTranslation("common");

    return (
        <AlertDialog
            open={isStatusDialogOpen}
            onOpenChange={(open) => {
                setIsStatusDialogOpen(open);
                if (!open) onStatusTargetChange({ user: null, nextState: false });
            }}
        >
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>
                        {statusTarget.nextState ? t("status.activateTitle") : t("status.deactivateTitle")}
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
                        className={statusTarget.nextState ? "" : "bg-destructive text-white hover:bg-destructive/90"}
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
    );
}
