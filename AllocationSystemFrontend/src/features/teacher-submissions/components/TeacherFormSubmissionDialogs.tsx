// components
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";

import { SubmissionDataView } from "./SubmissionDataView";
// translation
import { useTranslation } from "react-i18next";
// types
import type { TeacherFormSubmission } from "../types/teacherFormSubmission.types";
// icons
import { Loader2 } from "lucide-react";

interface TeacherFormSubmissionDialogsProps {
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isStatusDialogOpen: boolean;
  setIsStatusDialogOpen: (open: boolean) => void;
  selectedSubmission: TeacherFormSubmission | null;
  statusTarget: { submission: TeacherFormSubmission | null; nextStatus: boolean };
  setStatusTarget: (target: { submission: TeacherFormSubmission | null; nextStatus: boolean }) => void;
  onStatusChange: () => Promise<void>;
  isSubmitting: boolean;
}

export function TeacherFormSubmissionDialogs({
  isViewDialogOpen,
  setIsViewDialogOpen,
  isStatusDialogOpen,
  setIsStatusDialogOpen,
  selectedSubmission,
  statusTarget,
  setStatusTarget,
  onStatusChange,
  isSubmitting,
}: TeacherFormSubmissionDialogsProps) {
  const { t } = useTranslation("teacherSubmissions");
  const tCommon = useTranslation("common").t;

  const handleConfirmStatusChange = async () => {
      await onStatusChange();
      setIsStatusDialogOpen(false);
      setStatusTarget({ submission: null, nextStatus: false });
  };

  return (
    <>
      {/* View Dialog */}
      <ViewDialog
        open={isViewDialogOpen}
        onOpenChange={setIsViewDialogOpen}
        data={selectedSubmission}
        title={t("form.title.view")}
        description={t("form.description")}
        maxWidth="4xl"
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(submission) => {
          if (!submission) return null;
          return <DialogBody><SubmissionDataView submission={submission} /></DialogBody>;
        }}
        onEdit={
          selectedSubmission
            ? () => {
                setIsViewDialogOpen(false);
                if (selectedSubmission.isProcessed) {
                  setStatusTarget({ submission: selectedSubmission, nextStatus: false });
                } else {
                  setStatusTarget({ submission: selectedSubmission, nextStatus: true });
                }
                setIsStatusDialogOpen(true);
              }
            : undefined
        }
        editLabel={
          selectedSubmission?.isProcessed
            ? t("actions.markUnprocessed")
            : t("actions.markProcessed")
        }
      />

      {/* Status Change Confirmation Dialog */}
      <Dialog open={isStatusDialogOpen} onOpenChange={setIsStatusDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("statusDialog.title")}</DialogTitle>
            <DialogDescription>
              {statusTarget.nextStatus
                ? t("statusDialog.messageProcessed")
                : t("statusDialog.messageUnprocessed")}
            </DialogDescription>
          </DialogHeader>
          <DialogBody>
            {statusTarget.submission && (
              <div className="py-4">
                <p className="text-sm text-muted-foreground">
                  <strong>{t("statusDialog.teacher")}:</strong>{" "}
                  {statusTarget.submission.teacherFirstName}{" "}
                  {statusTarget.submission.teacherLastName}
                </p>
                <p className="text-sm text-muted-foreground mt-1">
                  <strong>{t("statusDialog.academicYear")}:</strong>{" "}
                  {statusTarget.submission.yearName}
                </p>
              </div>
            )}
          </DialogBody>
          <DialogFooter className="p-2">
            <Button
              variant="outline"
              onClick={() => {
                setIsStatusDialogOpen(false);
                setStatusTarget({ submission: null, nextStatus: false });
              }}
              disabled={isSubmitting}
            >
              {tCommon("actions.cancel")}
            </Button>
            <Button onClick={handleConfirmStatusChange} disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {tCommon("actions.confirm")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

