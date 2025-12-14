import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";
import { AllocationPlanForm } from "./AllocationPlanForm";

interface AllocationPlanDialogsProps {
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
  selectedAllocationPlan: AllocationPlan | null;

  // Handlers
  onCreateSubmit: (
    data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest
  ) => Promise<void>;
  onUpdateSubmit: (
    data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest
  ) => Promise<void>;
  onDelete: () => void;
  onEditClick: (allocationPlan: AllocationPlan) => void;
  onSelectedChange: (allocationPlan: AllocationPlan | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"allocationPlans">;
}

export function AllocationPlanDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedAllocationPlan,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: AllocationPlanDialogsProps) {
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
            <AllocationPlanForm
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
        data={selectedAllocationPlan}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedAllocationPlan) {
            onEditClick(selectedAllocationPlan);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(plan) => (
          <DialogBody>
            <div className="space-y-4 py-4">
              <div className="grid gap-4 md:grid-cols-2">
                <ReadOnlyField label={t("form.fields.planName")} value={plan.planName} />
                <ReadOnlyField label={t("form.fields.planVersion")} value={plan.planVersion} />
                <ReadOnlyField label={t("form.fields.yearName")} value={plan.yearName} />
                <ReadOnlyField label={t("form.fields.status")} value={plan.statusDisplayName} />
                <ReadOnlyField
                  label={t("form.fields.isCurrent")}
                  value={plan.isCurrent ? t("table.current") : t("table.notCurrent")}
                />
                <ReadOnlyField
                  label={t("form.fields.createdAt")}
                  value={plan.createdAt ? new Date(plan.createdAt).toLocaleString() : "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.updatedAt")}
                  value={plan.updatedAt ? new Date(plan.updatedAt).toLocaleString() : "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.notes")}
                  value={plan.notes ?? "-"}
                  className="space-y-2 md:col-span-2"
                  valueClassName="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 min-h-[48px]"
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
            {selectedAllocationPlan && (
              <AllocationPlanForm
                key={`edit-${selectedAllocationPlan.id}`}
                allocationPlan={selectedAllocationPlan}
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
