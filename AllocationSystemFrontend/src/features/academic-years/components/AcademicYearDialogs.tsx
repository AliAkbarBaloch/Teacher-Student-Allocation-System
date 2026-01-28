import { ViewDialog } from "@/components/common/ViewDialog";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { AcademicYearForm } from "./AcademicYearForm";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";
import { Loader2 } from "lucide-react";
import { formatDate } from "@/lib/utils/date";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";

/**
 * Properties for the AcademicYearDialogs component.
 * Contains dialog state flags and handler callbacks.
 */
interface AcademicYearDialogsProps {
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  isEditDialogOpen: boolean;
  setIsEditDialogOpen: (open: boolean) => void;
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isDeleteDialogOpen: boolean;
  setIsDeleteDialogOpen: (open: boolean) => void;

  selectedAcademicYear: AcademicYear | null;

  onCreateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (academicYear: AcademicYear) => void;

  isSubmitting: boolean;
  t: TFunction<"academicYears">;
}

const FORM_DESCRIPTION_KEY = "form.description";

function renderCreateDialog(
  isCreateDialogOpen: boolean,
  setIsCreateDialogOpen: (open: boolean) => void,
  onCreateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>,
  isSubmitting: boolean,
  t: TFunction<"academicYears">
) {
  return (
    <CreateAcademicYearDialog
      open={isCreateDialogOpen}
      onOpenChange={setIsCreateDialogOpen}
      onSubmit={onCreateSubmit}
      isSubmitting={isSubmitting}
      t={t}
    />
  );
}

function renderEditDialog(
  isEditDialogOpen: boolean,
  setIsEditDialogOpen: (open: boolean) => void,
  selectedAcademicYear: AcademicYear | null,
  onUpdateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>,
  isSubmitting: boolean,
  t: TFunction<"academicYears">
) {
  return (
    <EditAcademicYearDialog
      open={isEditDialogOpen}
      onOpenChange={setIsEditDialogOpen}
      academicYear={selectedAcademicYear}
      onSubmit={onUpdateSubmit}
      isSubmitting={isSubmitting}
      t={t}
    />
  );
}

function renderViewDialog(
  isViewDialogOpen: boolean,
  setIsViewDialogOpen: (open: boolean) => void,
  selectedAcademicYear: AcademicYear | null,
  onEditClick: (academicYear: AcademicYear) => void,
  t: TFunction<"academicYears">
) {
  return (
    <AcademicYearViewDialog
      open={isViewDialogOpen}
      onOpenChange={setIsViewDialogOpen}
      academicYear={selectedAcademicYear}
      onEditClick={onEditClick}
      t={t}
    />
  );
}

function renderDeleteDialog(
  isDeleteDialogOpen: boolean,
  setIsDeleteDialogOpen: (open: boolean) => void,
  onDelete: () => void,
  isSubmitting: boolean,
  t: TFunction<"academicYears">
) {
  return (
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
  );
}

function AcademicYearDialogsContent(props: AcademicYearDialogsProps) {
  return (
    <>
      {renderCreateDialog(props.isCreateDialogOpen, props.setIsCreateDialogOpen, props.onCreateSubmit, props.isSubmitting, props.t)}
      {renderEditDialog(props.isEditDialogOpen, props.setIsEditDialogOpen, props.selectedAcademicYear, props.onUpdateSubmit, props.isSubmitting, props.t)}
      {renderViewDialog(props.isViewDialogOpen, props.setIsViewDialogOpen, props.selectedAcademicYear, props.onEditClick, props.t)}
      {renderDeleteDialog(props.isDeleteDialogOpen, props.setIsDeleteDialogOpen, props.onDelete, props.isSubmitting, props.t)}
    </>
  );
}


function CreateAcademicYearDialog(props: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  isSubmitting: boolean;
  t: TFunction<"academicYears">;
}) {
  const { open, onOpenChange, onSubmit, isSubmitting, t } = props;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>{t("form.title.create")}</DialogTitle>
          <DialogDescription>{t(FORM_DESCRIPTION_KEY)}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          <AcademicYearForm
            onSubmit={onSubmit}
            onCancel={() => onOpenChange(false)}
            isLoading={isSubmitting}
          />
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}

function EditAcademicYearDialog(props: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  academicYear: AcademicYear | null;
  onSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  isSubmitting: boolean;
  t: TFunction<"academicYears">;
}) {
  const { open, onOpenChange, academicYear, onSubmit, isSubmitting, t } = props;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>{t("form.title.edit")}</DialogTitle>
          <DialogDescription>{t(FORM_DESCRIPTION_KEY)}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          {isSubmitting ? (
            <div className="flex min-h-[200px] items-center justify-center">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : (
            academicYear && (
              <AcademicYearForm
                key={`edit-${academicYear.id}`}
                academicYear={academicYear}
                onSubmit={onSubmit}
                onCancel={() => onOpenChange(false)}
                isLoading={isSubmitting}
              />
            )
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  );
}

function AcademicYearViewContent(props: {
  ay: AcademicYear;
  t: TFunction<"academicYears">;
}) {
  const { ay, t } = props;

  return (
    <DialogBody>
      <div className="grid lg:grid-cols-2 gap-4">
        <ReadOnlyField label={t("form.fields.yearName")} value={ay.yearName} />
        <ReadOnlyField label={t("form.fields.totalCreditHours")} value={ay.totalCreditHours} />
        <ReadOnlyField label={t("form.fields.elementarySchoolHours")} value={ay.elementarySchoolHours} />
        <ReadOnlyField label={t("form.fields.middleSchoolHours")} value={ay.middleSchoolHours} />
        <ReadOnlyField label={t("form.fields.allocationDeadline")} value={ay.allocationDeadline ?? "—"} />
        <ReadOnlyField
          label={t("form.fields.isLocked")}
          value={ay.isLocked ? t("table.locked") : t("table.unlocked")}
        />
        <ReadOnlyField
          label={t("form.fields.createdAt")}
          value={ay.createdAt ? formatDate(ay.createdAt) : "—"}
        />
        <ReadOnlyField
          label={t("form.fields.updatedAt")}
          value={ay.updatedAt ? formatDate(ay.updatedAt) : "—"}
        />
      </div>
    </DialogBody>
  );
}
function handleViewEdit(
  academicYear: AcademicYear | null,
  onOpenChange: (open: boolean) => void,
  onEditClick: (ay: AcademicYear) => void
) {
  onOpenChange(false);
  if (academicYear) {
    onEditClick(academicYear);
  }
}

function AcademicYearViewDialog(props: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  academicYear: AcademicYear | null;
  onEditClick: (ay: AcademicYear) => void;
  t: TFunction<"academicYears">;
}) {
  const { open, onOpenChange, academicYear, onEditClick, t } = props;
  const { t: tCommon } = useTranslation("common");

  return (
    <ViewDialog
      open={open}
      onOpenChange={onOpenChange}
      data={academicYear}
      title={t("form.title.view")}
      description={t(FORM_DESCRIPTION_KEY)}
      maxWidth="2xl"
      onEdit={() => handleViewEdit(academicYear, onOpenChange, onEditClick)}
      editLabel={tCommon("actions.edit")}
      closeLabel={tCommon("actions.close")}
      renderCustomContent={(ay) => <AcademicYearViewContent ay={ay} t={t} />}
    />
  );
}

export function AcademicYearDialogs(props: AcademicYearDialogsProps) {
  return <AcademicYearDialogsContent {...props} />;
}