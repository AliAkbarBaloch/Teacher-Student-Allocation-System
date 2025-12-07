import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { PlanChangeLogForm } from "./PlanChangeLogsForm";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface PlanChangeLogsDialogsProps {
  // Dialog states
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  isEditDialogOpen: boolean;
  setIsEditDialogOpen: (open: boolean) => void;
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isDeleteDialogOpen: boolean;
  setIsDeleteDialogOpen: (open: boolean) => void;

  // Data
  selectedPlanChangeLog: PlanChangeLog | null;

  // Handlers
  onCreateSubmit: (data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest) => Promise<void>;
  onUpdateSubmit: (data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (log: PlanChangeLog) => void;
  onSelectedChange: (log: PlanChangeLog | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"planChangeLogs">;
}

export function PlanChangeLogsDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedPlanChangeLog,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: PlanChangeLogsDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            <PlanChangeLogForm
              onSubmit={onCreateSubmit}
              onCancel={() => setIsCreateDialogOpen(false)}
              isLoading={isSubmitting}
            />
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* View Dialog (Read-only) */}
      <ViewDialog
        open={isViewDialogOpen}
        onOpenChange={(open) => {
          setIsViewDialogOpen(open);
          if (!open) {
            onSelectedChange(null);
          }
        }}
        data={selectedPlanChangeLog}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedPlanChangeLog) {
            onEditClick(selectedPlanChangeLog);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(log) => (
          <DialogBody>
            <div className="space-y-4 py-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.planId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.planId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.changeType")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.changeType}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.entityType")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.entityType}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.entityId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.entityId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.oldValue")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.oldValue ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.newValue")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.newValue ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.reason")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.reason ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.createdAt")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.createdAt}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.updatedAt")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {log.updatedAt ?? "-"}
                  </div>
                </div>
              </div>
            </div>
          </DialogBody>
        )}
      />

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {selectedPlanChangeLog && (
              <PlanChangeLogForm
                key={`edit-${selectedPlanChangeLog.id}`}
                planChangeLog={selectedPlanChangeLog}
                onSubmit={onUpdateSubmit}
                onCancel={() => {
                  setIsEditDialogOpen(false);
                  onSelectedChange(null);
                }}
                isLoading={isSubmitting}
              />
            )}
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <DeleteConfirmationDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={onDelete}
        title={t("delete.title")}
        description={t("delete.message")}
        cancelLabel={t("delete.cancel")}
        confirmLabel={t("delete.confirm")}
        isSubmitting={isSubmitting}
      />
    </>
  );
}