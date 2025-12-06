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
  onCreateSubmit: (data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest) => Promise<void>;
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
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.academicYear")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.academicYearTitle}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.teacher")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.teacherTitle}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.subjectCode")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.subjectId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.subjectTitle")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.subjectTitle}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.availabilityStatus")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.availabilityStatus?.toLowerCase().split("_").join(" ").replace("available", t("table.available")).replace("not_available", t("table.notAvailable")).replace("limited", t("table.limited")).replace("preferred", t("table.preferred"))}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.gradeLevelFrom")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.gradeLevelFrom ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.gradeLevelTo")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.gradeLevelTo ?? "-"}
                  </div>
                </div>
                <div className="space-y-2 md:col-span-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.notes")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.notes || "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.createdAt")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.createdAt
                      ? new Date(teacherSubject.createdAt).toLocaleString()
                      : "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.updatedAt")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {teacherSubject.updatedAt
                      ? new Date(teacherSubject.updatedAt).toLocaleString()
                      : "-"}
                  </div>
                </div>
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