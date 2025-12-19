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
// forms
import { InternshipTypeForm } from "./InternshipTypeForm";
// types
import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
} from "../types/internshipType.types";
// translations
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";

interface InternshipTypeDialogsProps {
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
  selectedInternshipType: InternshipType | null;

  // Handlers
  onCreateSubmit: (data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (internshipType: InternshipType) => void;
  onSelectedChange: (internshipType: InternshipType | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"internshipTypes">;
}

export function InternshipTypeDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedInternshipType,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: InternshipTypeDialogsProps) {
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
            <InternshipTypeForm
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
        data={selectedInternshipType}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedInternshipType) {
            onEditClick(selectedInternshipType);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(internshipType) => (
          <DialogBody>
            <div className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                  <ReadOnlyField
                    label={t("form.fields.code")}
                    value={internshipType.internshipCode}
                  />
                  <ReadOnlyField
                    label={t("form.fields.fullName")}
                    value={internshipType.fullName}
                  />

                  <ReadOnlyField
                    label={t("form.fields.timing")}
                    value={internshipType.timing || "-"}
                  />
                  <ReadOnlyField
                    label={t("form.fields.periodType")}
                    value={internshipType.periodType || "-"}
                  />
                  <ReadOnlyField
                    label={t("form.fields.semester")}
                    value={internshipType.semester || "-"}
                  />
                  <ReadOnlyField
                    label={t("form.fields.priorityOrder")}
                    value={internshipType.priorityOrder ?? "-"}
                  />
                  <ReadOnlyField
                    label={t("form.fields.isSubjectSpecific")}
                    value={internshipType.isSubjectSpecific ? t("table.yes") : t("table.no")}
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
            {selectedInternshipType && (
              <InternshipTypeForm
                key={`edit-${selectedInternshipType.id}`}
                internshipType={selectedInternshipType}
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

