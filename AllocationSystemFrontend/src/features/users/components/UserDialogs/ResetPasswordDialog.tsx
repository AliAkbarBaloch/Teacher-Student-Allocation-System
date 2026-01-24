import { useEffect, useState } from "react";
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

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

import type { UserDialogsProps } from "./UserDialogs.types";

type Props = Pick<
    UserDialogsProps,
    | "isResetPasswordDialogOpen"
    | "setIsResetPasswordDialogOpen"
    | "resetTarget"
    | "onResetPassword"
    | "onResetTargetChange"
    | "isSubmitting"
>;

export function ResetPasswordDialog({
    isResetPasswordDialogOpen,
    setIsResetPasswordDialogOpen,
    resetTarget,
    onResetPassword,
    onResetTargetChange,
    isSubmitting,
}: Props) {
    const { t } = useTranslation("users");
    const { t: tCommon } = useTranslation("common");

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [resetError, setResetError] = useState<string | null>(null);

    useEffect(() => {
        if (!isResetPasswordDialogOpen) {
            setNewPassword("");
            setConfirmPassword("");
            setResetError(null);
        }
    }, [isResetPasswordDialogOpen]);

    const handleConfirmResetPassword = async () => {
        setResetError(null);

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
            //
        }
    };

    return (
        <Dialog
            open={isResetPasswordDialogOpen}
            onOpenChange={(open) => {
                setIsResetPasswordDialogOpen(open);
                if (!open) onResetTargetChange(null);
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

                        <p className="text-xs text-muted-foreground">{t("resetPassword.hint")}</p>

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
                                {isSubmitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                                {t("resetPassword.actions.confirm")}
                            </Button>
                        </div>
                    </div>
                </DialogBody>
            </DialogContent>
        </Dialog>
    );
}
