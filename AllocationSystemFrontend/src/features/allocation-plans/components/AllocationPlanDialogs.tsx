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

/**
 * Props for the AllocationPlanDialogs component.
 * Controls which dialog is open and provides handlers for each action.
 */
interface AllocationPlanDialogsProps {
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  isEditDialogOpen: boolean;
  setIsEditDialogOpen: (open: boolean) => void;
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isDeleteDialogOpen: boolean;
  setIsDeleteDialogOpen: (open: boolean) => void;

  selectedAllocationPlan: AllocationPlan | null;

  onCreateSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (allocationPlan: AllocationPlan) => void;
  onSelectedChange: (allocationPlan: AllocationPlan | null) => void;

  isSubmitting: boolean;

  t: TFunction<"allocationPlans">;
}

/**
 * Props shared by all allocation plan dialog components.
 */
interface AllocationPlanDialogCommonProps {
  isSubmitting: boolean;
  t: TFunction<"allocationPlans">;
  tCommon: TFunction<"common">;
}

/**
 * Props for the create allocation plan dialog.
 */
interface CreateAllocationPlanDialogProps extends AllocationPlanDialogCommonProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
}

/**
 * Props for the view allocation plan dialog.
 */
interface ViewAllocationPlanDialogProps extends AllocationPlanDialogCommonProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  selectedAllocationPlan: AllocationPlan | null;
  onEditClick: (allocationPlan: AllocationPlan) => void;
  onSelectedChange: (allocationPlan: AllocationPlan | null) => void;
}

/**
 * Props for the edit allocation plan dialog.
 */
interface EditAllocationPlanDialogProps extends AllocationPlanDialogCommonProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  selectedAllocationPlan: AllocationPlan | null;
  onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  onSelectedChange: (allocationPlan: AllocationPlan | null) => void;
}

/**
 * Props for the delete allocation plan dialog.
 */
interface DeleteAllocationPlanDialogProps extends AllocationPlanDialogCommonProps {
  open: boolean;
  setOpen: (open: boolean) => void;
  onDelete: () => void;
}

function CreateAllocationPlanDialog({ open, setOpen, onSubmit, isSubmitting, t }: CreateAllocationPlanDialogProps) {
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("form.title.create")}</DialogTitle>
          <DialogDescription>{t("subtitle")}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          <AllocationPlanForm onSubmit={onSubmit} onCancel={() => setOpen(false)} isLoading={isSubmitting} />
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}

/**
 * Read-only notes field shown in the allocation plan view.
 */
function AllocationPlanNotesField(props: { value: string; label: string }) {
  return (
    <ReadOnlyField
      label={props.label}
      value={props.value}
      className="space-y-2 md:col-span-2"
      valueClassName="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 min-h-[48px]"
    />
  );
}

/**
 * Read-only details shown inside the allocation plan view dialog.
 */
function AllocationPlanViewContent(props: { plan: AllocationPlan; t: TFunction<"allocationPlans"> }) {
  const { plan, t } = props;
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
          <AllocationPlanNotesField label={t("form.fields.notes")} value={plan.notes ?? "-"} />
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

function handleViewDialogOpenChange(
  isOpen: boolean,
  setOpen: (open: boolean) => void,
  onSelectedChange: (allocationPlan: AllocationPlan | null) => void
) {
  setOpen(isOpen);
  if (!isOpen) {
    onSelectedChange(null);
  }
}

function ViewAllocationPlanDialog({
  open,
  setOpen,
  selectedAllocationPlan,
  onEditClick,
  onSelectedChange,
  t,
  tCommon,
}: ViewAllocationPlanDialogProps) {
  return (
    <ViewDialog
      open={open}
      onOpenChange={(isOpen) => handleViewDialogOpenChange(isOpen, setOpen, onSelectedChange)}
      data={selectedAllocationPlan}
      title={t("form.title.view")}
      description={t("subtitle")}
      maxWidth="2xl"
      onEdit={() => {
        setOpen(false);
        if (selectedAllocationPlan) onEditClick(selectedAllocationPlan);
      }}
      editLabel={tCommon("actions.edit")}
      closeLabel={tCommon("actions.close")}
      renderCustomContent={(plan) => <AllocationPlanViewContent plan={plan} t={t} />}
    />
  );
}

function EditAllocationPlanDialog({
  open,
  setOpen,
  selectedAllocationPlan,
  onSubmit,
  onSelectedChange,
  isSubmitting,
  t,
}: EditAllocationPlanDialogProps) {
  return (
    <Dialog open={open} onOpenChange={setOpen}>
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
              onSubmit={onSubmit}
              onCancel={() => {
                setOpen(false);
                onSelectedChange(null);
              }}
              isLoading={isSubmitting}
            />
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}

function DeleteAllocationPlanDialog({
  open,
  setOpen,
  onDelete,
  isSubmitting,
  t,
}: DeleteAllocationPlanDialogProps) {
  return (
    <DeleteConfirmationDialog
      open={open}
      onOpenChange={setOpen}
      onConfirm={onDelete}
      title={t("delete.title")}
      description={t("delete.message")}
      cancelLabel={t("delete.cancel")}
      confirmLabel={t("delete.confirm")}
      isSubmitting={isSubmitting}
    />
  );
}

export function AllocationPlanDialogs(props: AllocationPlanDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  const common: AllocationPlanDialogCommonProps = { isSubmitting: props.isSubmitting, t: props.t, tCommon };

  return (
    <>
      <CreateAllocationPlanDialog open={props.isCreateDialogOpen} setOpen={props.setIsCreateDialogOpen} onSubmit={props.onCreateSubmit} {...common} />
      <ViewAllocationPlanDialog open={props.isViewDialogOpen} setOpen={props.setIsViewDialogOpen} selectedAllocationPlan={props.selectedAllocationPlan} onEditClick={props.onEditClick} onSelectedChange={props.onSelectedChange} {...common} />
      <EditAllocationPlanDialog open={props.isEditDialogOpen} setOpen={props.setIsEditDialogOpen} selectedAllocationPlan={props.selectedAllocationPlan} onSubmit={props.onUpdateSubmit} onSelectedChange={props.onSelectedChange} {...common} />
      <DeleteAllocationPlanDialog open={props.isDeleteDialogOpen} setOpen={props.setIsDeleteDialogOpen} onDelete={props.onDelete} {...common} />
    </>
  );
}