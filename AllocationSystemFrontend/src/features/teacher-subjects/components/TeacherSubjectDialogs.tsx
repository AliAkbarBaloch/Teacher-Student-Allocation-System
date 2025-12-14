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
import { TeacherSubjectForm } from "./TeacherSubjectForm";
import type {
  TeacherSubject,
  CreateTeacherSubjectRequest,
  UpdateTeacherSubjectRequest,
} from "../types/teacherSubject.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";
import { formatDate } from "@/lib/utils/date";

interface TeacherSubjectDialogsProps {
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
  selectedTeacherSubject: TeacherSubject | null;

  // Handlers
  onCreateSubmit: (
    data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest
  ) => Promise<void>;
  onUpdateSubmit: (
    data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest
  ) => Promise<void>;
  onDelete: () => void;
  onEditClick: (teacherSubject: TeacherSubject) => void;
  onSelectedChange: (teacherSubject: TeacherSubject | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"teacherSubjects">;
}

export function TeacherSubjectDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedTeacherSubject,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: TeacherSubjectDialogsProps) {
  const { t: tCommon } = useTranslation("common");

  const getAvailabilityStatusLabel = (
    status: string | null | undefined
  ): string => {
    if (!status) return t("table.available");

    switch (status) {
      case "AVAILABLE":
        return t("table.available");
      case "NOT_AVAILABLE":
        return t("table.notAvailable");
      case "LIMITED":
        return t("table.limited");
      case "PREFERRED":
        return t("table.preferred");
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
            <TeacherSubjectForm
              onSubmit={(data) =>
                onCreateSubmit(data as CreateTeacherSubjectRequest)
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
        data={selectedTeacherSubject}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedTeacherSubject) {
            onEditClick(selectedTeacherSubject);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(teacherSubject) => (
          <DialogBody>
            <div className="space-y-4 py-4">
              <div className="grid gap-4 md:grid-cols-2">
                <ReadOnlyField
                  label={t("form.fields.academicYear")}
                  value={teacherSubject.academicYearTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.teacher")}
                  value={teacherSubject.teacherTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.subjectCode")}
                  value={teacherSubject.subjectId}
                />
                <ReadOnlyField
                  label={t("form.fields.subjectTitle")}
                  value={teacherSubject.subjectTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.availabilityStatus")}
                  value={getAvailabilityStatusLabel(
                    teacherSubject.availabilityStatus
                  )}
                />
                <ReadOnlyField
                  label={t("form.fields.gradeLevelFrom")}
                  value={teacherSubject.gradeLevelFrom ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.gradeLevelTo")}
                  value={teacherSubject.gradeLevelTo ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.notes")}
                  value={teacherSubject.notes ?? "-"}
                />
                <ReadOnlyField
                  label={t("form.fields.createdAt")}
                  value={
                    teacherSubject.createdAt
                      ? formatDate(teacherSubject.createdAt)
                      : "-"
                  }
                />
                <ReadOnlyField
                  label={t("form.fields.updatedAt")}
                  value={
                    teacherSubject.updatedAt
                      ? formatDate(teacherSubject.updatedAt)
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
            {selectedTeacherSubject && (
              <TeacherSubjectForm
                key={`edit-${selectedTeacherSubject.id}`}
                teacherSubject={selectedTeacherSubject}
                onSubmit={(data) =>
                  onUpdateSubmit(data as UpdateTeacherSubjectRequest)
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
