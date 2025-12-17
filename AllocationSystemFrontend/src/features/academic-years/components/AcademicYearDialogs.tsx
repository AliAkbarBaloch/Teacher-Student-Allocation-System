
import { ViewDialog } from "@/components/common/ViewDialog";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";
import { Dialog } from "@/components/ui/dialog";
import { DialogContent } from "@/components/ui/dialog";
import { DialogHeader } from "@/components/ui/dialog";
import { DialogTitle } from "@/components/ui/dialog";
import { DialogDescription } from "@/components/ui/dialog";
import { DialogBody } from "@/components/ui/dialog";
import { AcademicYearForm } from "./AcademicYearForm";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";
import { Loader2 } from "lucide-react";
import { formatDate } from "@/lib/utils/date";

import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";

interface AcademicYearDialogsProps {
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
  selectedAcademicYear: AcademicYear | null;

  // Handlers
  onCreateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (academicYear: AcademicYear) => void;
  onSelectedChange: (academicYear: AcademicYear | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"academicYears">;
}

export function AcademicYearDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedAcademicYear,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  isSubmitting,
  t,
}: AcademicYearDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  

  return (
    <>
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            <AcademicYearForm
              onSubmit={onCreateSubmit}
              onCancel={() => setIsCreateDialogOpen(false)}
              isLoading={isSubmitting}
            />
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {isSubmitting ? (
              <div className="flex min-h-[200px] items-center justify-center">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : (
                selectedAcademicYear && (
                <AcademicYearForm
                  key={`edit-${selectedAcademicYear.id}`}
                  academicYear={selectedAcademicYear}
                  onSubmit={onUpdateSubmit}
                  onCancel={() => setIsEditDialogOpen(false)}
                  isLoading={isSubmitting}
                />
              )
            )}
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* View Dialog */}
      <ViewDialog
        open={isViewDialogOpen}
        onOpenChange={setIsViewDialogOpen}
        data={selectedAcademicYear}
        title={t("form.title.view")}
        description={t("form.description")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedAcademicYear) {
            onEditClick(selectedAcademicYear);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(academicYear) => (
          <DialogBody>
            <div className="grid lg:grid-cols-2 gap-4">
              <ReadOnlyField
                label={t("form.fields.yearName")}
                value={academicYear.yearName}
              />
              <ReadOnlyField
                label={t("form.fields.totalCreditHours")}
                value={academicYear.totalCreditHours}
              />
              <ReadOnlyField
                label={t("form.fields.elementarySchoolHours")}
                value={academicYear.elementarySchoolHours}
              />
              <ReadOnlyField
                label={t("form.fields.middleSchoolHours")}
                value={academicYear.middleSchoolHours}
              />
              <ReadOnlyField
                label={t("form.fields.allocationDeadline")}
                value={academicYear.allocationDeadline ?? "—"}
              />
              <ReadOnlyField
                label={t("form.fields.isLocked")}
                value={academicYear.isLocked ? t("table.locked") : t("table.unlocked")}
              />
              <ReadOnlyField
                label={t("form.fields.createdAt")}
                value={academicYear.createdAt ? formatDate(academicYear.createdAt) : "—"}
              />
              <ReadOnlyField
                label={t("form.fields.updatedAt")}
                value={academicYear.updatedAt ? formatDate(academicYear.updatedAt) : "—"}
              />
            </div>
          </DialogBody>
        )}
      />

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