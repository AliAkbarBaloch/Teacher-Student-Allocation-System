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
import type { CrudDialogState } from "@/types/dialog.types";
import { AllocationPlanForm } from "./AllocationPlanForm";

/**
 * Props for the AllocationPlanDialogs component
 */
interface AllocationPlanDialogsProps extends CrudDialogState {

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
      <CreateAllocationPlanDialog
        isOpen={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        onSubmit={onCreateSubmit}
        isSubmitting={isSubmitting}
        t={t}
      />

      <AllocationPlanViewDialog
        isOpen={isViewDialogOpen}
        onOpenChange={setIsViewDialogOpen}
        selectedPlan={selectedAllocationPlan}
        onSelectedChange={onSelectedChange}
        onEditClick={onEditClick}
        t={t}
        tCommon={tCommon}
      />

      <EditAllocationPlanDialog
        isOpen={isEditDialogOpen}
        onOpenChange={setIsEditDialogOpen}
        selectedPlan={selectedAllocationPlan}
        onSubmit={onUpdateSubmit}
        onCancel={() => {
          setIsEditDialogOpen(false);
          onSelectedChange(null);
        }}
        isSubmitting={isSubmitting}
        t={t}
      />

      <AllocationPlanDeleteDialog
        isOpen={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={onDelete}
        isSubmitting={isSubmitting}
        t={t}
      />
    </>
  );
}

/**
 * Content for the Allocation Plan view dialog
 */
function AllocationPlanViewContent({ plan, t }: { plan: AllocationPlan; t: TFunction<"allocationPlans"> }) {
  return (
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
            label={t("form.fields.notes")}
            value={plan.notes ?? "-"}
            className="space-y-2 md:col-span-2"
            valueClassName="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 min-h-[48px]"
          />
          <ReadOnlyField
            label={t("form.fields.createdAt")}
            value={plan.createdAt ? new Date(plan.createdAt).toLocaleString() : "-"}
          />
          <ReadOnlyField
            label={t("form.fields.updatedAt")}
            value={plan.updatedAt ? new Date(plan.updatedAt).toLocaleString() : "-"}
          />
        </div>
      </div>
    </DialogBody>
  );
}

/**
 * Dialog for creating a new Allocation Plan
 */
function CreateAllocationPlanDialog({
  isOpen,
  onOpenChange,
  onSubmit,
  isSubmitting,
  t,
}: {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  isSubmitting: boolean;
  t: TFunction<"allocationPlans">;
}) {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("form.title.create")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          <AllocationPlanForm
            onSubmit={onSubmit}
            onCancel={() => onOpenChange(false)}
            isLoading={isSubmitting}
          />
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}

/**
 * Dialog for viewing Allocation Plan details
 */
function AllocationPlanViewDialog({
  isOpen,
  onOpenChange,
  selectedPlan,
  onSelectedChange,
  onEditClick,
  t,
  tCommon,
}: {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  selectedPlan: AllocationPlan | null;
  onSelectedChange: (plan: AllocationPlan | null) => void;
  onEditClick: (plan: AllocationPlan) => void;
  t: TFunction<"allocationPlans">;
  tCommon: TFunction<"common">;
}) {
  return (
    <ViewDialog
      open={isOpen}
      onOpenChange={(open) => {
        onOpenChange(open);
        if (!open) {
          onSelectedChange(null);
        }
      }}
      data={selectedPlan}
      title={t("form.title.view")}
      description={t("subtitle")}
      maxWidth="2xl"
      onEdit={() => {
        onOpenChange(false);
        if (selectedPlan) {
          onEditClick(selectedPlan);
        }
      }}
      editLabel={tCommon("actions.edit")}
      closeLabel={tCommon("actions.close")}
      renderCustomContent={(plan) => <AllocationPlanViewContent plan={plan} t={t} />}
    />
  );
}

/**
 * Dialog for confirming Allocation Plan deletion
 */
function AllocationPlanDeleteDialog({
  isOpen,
  onOpenChange,
  onConfirm,
  isSubmitting,
  t,
}: {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: () => void;
  isSubmitting: boolean;
  t: TFunction<"allocationPlans">;
}) {
  return (
    <DeleteConfirmationDialog
      open={isOpen}
      onOpenChange={onOpenChange}
      onConfirm={onConfirm}
      title={t("delete.title")}
      description={t("delete.message")}
      cancelLabel={t("delete.cancel")}
      confirmLabel={t("delete.confirm")}
      isSubmitting={isSubmitting}
    />
  );
}

/**
 * Dialog for editing an existing Allocation Plan
 */
function EditAllocationPlanDialog({
  isOpen,
  onOpenChange,
  selectedPlan,
  onSubmit,
  onCancel,
  isSubmitting,
  t,
}: {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  selectedPlan: AllocationPlan | null;
  onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
  t: TFunction<"allocationPlans">;
}) {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("form.title.edit")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          {selectedPlan && (
            <AllocationPlanForm
              key={`edit-${selectedPlan.id}`}
              allocationPlan={selectedPlan}
              onSubmit={onSubmit}
              onCancel={onCancel}
              isLoading={isSubmitting}
            />
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}