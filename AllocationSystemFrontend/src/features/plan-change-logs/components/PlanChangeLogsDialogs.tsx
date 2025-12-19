// components
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
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";

// forms
import { PlanChangeLogForm } from "./PlanChangeLogsForm";

// types
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";

// translations
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
                <ReadOnlyField
                  label={t("form.fields.planId")}
                  value={log.planId}
                />
                <ReadOnlyField
                  label={t("form.fields.changeType")}
                  value={log.changeType}
                />
                <ReadOnlyField
                  label={t("form.fields.entityType")}
                  value={log.entityType}
                />
                <ReadOnlyField
                  label={t("form.fields.entityId")}
                  value={log.entityId}
                />
                <ReadOnlyField
                  label={t("form.fields.oldValue")}
                  value={log.oldValue ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.newValue")}
                  value={log.newValue ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.reason")}
                  value={log.reason ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.createdAt")}
                  value={log.createdAt}
                />
                <ReadOnlyField
                  label={t("form.fields.updatedAt")}
                  value={log.updatedAt ?? "-"}
                />
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