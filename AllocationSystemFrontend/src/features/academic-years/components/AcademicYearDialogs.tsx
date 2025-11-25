import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";
import { AcademicYearForm } from "./AcademicYearForm";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

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
  onSelectedChange,
  isSubmitting,
  t,
}: AcademicYearDialogsProps) {
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
          <AcademicYearForm
            onSubmit={onCreateSubmit}
            onCancel={() => setIsCreateDialogOpen(false)}
            isLoading={isSubmitting}
          />
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
        data={selectedAcademicYear}
        title={t("form.title.view")}
        description={t("subtitle")}
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
          <div className="space-y-4 py-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.yearName")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.yearName}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.totalCreditHours")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.totalCreditHours}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.elementarySchoolHours")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.elementarySchoolHours}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.middleSchoolHours")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.middleSchoolHours}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.budgetAnnouncementDate")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.budgetAnnouncementDate
                    ? new Date(academicYear.budgetAnnouncementDate).toLocaleString()
                    : "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.allocationDeadline")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.allocationDeadline
                    ? new Date(academicYear.allocationDeadline).toLocaleString()
                    : "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.isLocked")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.isLocked ? t("table.locked") : t("table.unlocked")}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.createdAt")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.createdAt
                    ? new Date(academicYear.createdAt).toLocaleString()
                    : "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.updatedAt")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {academicYear.updatedAt
                    ? new Date(academicYear.updatedAt).toLocaleString()
                    : "-"}
                </div>
              </div>
            </div>
          </div>
        )}
      />

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          {selectedAcademicYear && (
            <AcademicYearForm
              key={`edit-${selectedAcademicYear.id}`}
              academicYear={selectedAcademicYear}
              onSubmit={onUpdateSubmit}
              onCancel={() => {
                setIsEditDialogOpen(false);
                onSelectedChange(null);
              }}
              isLoading={isSubmitting}
            />
          )}
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