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
import { TeacherAvailabilityForm } from "./TeacherAvailabilityForm";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";

// types
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
} from "../types/teacherAvailability.types";

// translations
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

// hooks
import { formatDate } from "@/lib/utils/date";

interface TeacherAvailabilityDialogsProps {
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
  selectedTeacherAvailability: TeacherAvailability | null;

  // Handlers
  onCreateSubmit: (
    data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest
  ) => Promise<void>;
  onUpdateSubmit: (
    data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest
  ) => Promise<void>;
  onDelete: () => void;
  onEditClick: (availability: TeacherAvailability) => void;
  onSelectedChange: (availability: TeacherAvailability | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"teacherAvailability">;
}

export function TeacherAvailabilityDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedTeacherAvailability,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: TeacherAvailabilityDialogsProps) {
  const { t: tCommon } = useTranslation("common");

  const getAvailabilityStatusLabel = (
    status: string | null | undefined
  ): string => {
    if (!status) return t("table.available");

    switch (status) {
      case "AVAILABLE":
        return t("table.available");
      case "PREFERRED":
        return t("table.preferred");
      case "NOT_AVAILABLE":
        return t("table.notAvailable");
      case "BACKUP_ONLY":
        return t("table.backupOnly");
      default:
        return t("table.available");
    }
  };
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
            <TeacherAvailabilityForm
              onSubmit={(data) =>
                onCreateSubmit(data as CreateTeacherAvailabilityRequest)
              }
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
        data={selectedTeacherAvailability}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedTeacherAvailability) {
            onEditClick(selectedTeacherAvailability);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(availability) => (
          <DialogBody>
            <div className="space-y-4 pb-2">
              <div className="grid gap-4 md:grid-cols-2">
                <ReadOnlyField
                  label={t("form.fields.teacher")}
                  value={`${availability.teacherFirstName} ${availability.teacherLastName}`}
                />
                <ReadOnlyField
                  label={t("form.fields.academicYear")}
                  value={availability.academicYearName}
                />
                <ReadOnlyField
                  label={t("form.fields.internshipType")}
                  value={availability.internshipTypeName}
                />
                <ReadOnlyField
                  label={t("form.fields.isAvailable")}
                  value={getAvailabilityStatusLabel(availability.status)}
                />
                <ReadOnlyField
                  label={t("form.fields.preferenceRank")}
                  value={availability.preferenceRank ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.notes")}
                  value={availability.notes ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.createdAt")}
                  value={
                    availability.createdAt
                      ? formatDate(availability.createdAt)
                      : "-"
                  }
                />
                <ReadOnlyField
                  label={t("form.fields.updatedAt")}
                  value={
                    availability.updatedAt
                      ? formatDate(availability.updatedAt)
                      : "-"
                  }
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
            {selectedTeacherAvailability && (
              <TeacherAvailabilityForm
                key={`edit-${selectedTeacherAvailability.id}`}
                teacherAvailability={selectedTeacherAvailability}
                onSubmit={(data) =>
                  onUpdateSubmit(data as UpdateTeacherAvailabilityRequest)
                }
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
