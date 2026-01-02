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
import { Loader2 } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { CreateTeacherRequest, Teacher, UpdateTeacherRequest } from "../types/teacher.types";
import { TeacherForm } from "./TeacherForm";

interface TeacherDialogsProps {
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
  selectedTeacher: Teacher | null;
  formLoading: boolean;
  createFormKey: number;
  deleteTarget: Teacher | null;

  // Handlers
  onCreateSubmit: (payload: CreateTeacherRequest) => Promise<void>;
  onUpdateSubmit: (payload: UpdateTeacherRequest) => Promise<void>;
  onDelete: () => Promise<void>;
  onEdit?: () => void;

  // States
  isSubmitting: boolean;
  isAdmin: boolean;

  // Translations
  t: TFunction<"teachers">;
}

export function TeacherDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedTeacher,
  formLoading,
  createFormKey,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEdit,
  isSubmitting,
  isAdmin,
  t,
}: TeacherDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {isCreateDialogOpen && (
              <TeacherForm
                key={`create-teacher-form-${createFormKey}`}
                mode="create"
                onSubmit={onCreateSubmit}
                onCancel={() => setIsCreateDialogOpen(false)}
                isSubmitting={isSubmitting}
              />
            )}
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
            {formLoading ? (
              <div className="flex min-h-[200px] items-center justify-center">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : (
              selectedTeacher && (
                <TeacherForm
                  mode="edit"
                  teacher={selectedTeacher}
                  onSubmit={onUpdateSubmit}
                  onCancel={() => setIsEditDialogOpen(false)}
                  isSubmitting={isSubmitting}
                  readOnly={!isAdmin}
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
        data={selectedTeacher}
        title={t("form.title.view")}
        description={t("form.description")}
        maxWidth="2xl"
        onEdit={onEdit}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(teacher) => (
          <DialogBody>
            <div className="grid gap-4">
              <ReadOnlyField label={t("form.fields.firstName")} value={teacher.firstName} />
              <ReadOnlyField label={t("form.fields.lastName")} value={teacher.lastName} />
              <ReadOnlyField label={t("form.fields.email")} value={teacher.email} />
              <ReadOnlyField label={t("form.fields.school")} value={teacher.schoolName} />
              <ReadOnlyField label={t("form.fields.employmentStatus")} value={t(`${teacher.employmentStatus}`)} />
              <ReadOnlyField label={t("form.fields.isPartTime")} value={teacher.isPartTime ? t("table.yes") : t("table.no")} />
              <ReadOnlyField label={t("form.fields.workingHoursPerWeek")} value={teacher.workingHoursPerWeek} />
              <ReadOnlyField label={t("form.fields.usageCycle")} value={teacher.usageCycle ? t(`${teacher.usageCycle}`) : "-"} />

              <ReadOnlyField
                label = {t("form.fields.subjects")}
                value = {
                  teacher.subjects?.length
                  ? teacher.subjects?.map((s) => s.subjectTitle).join(", ")
                  : "-"
                }
              />

            </div>
          </DialogBody>
        )}
      />

      {/* Delete Confirmation */}
      <DeleteConfirmationDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={onDelete}
        title={t("delete.title")}
        description={t("delete.description")}
        cancelLabel={t("delete.cancel")}
        confirmLabel={t("delete.confirm")}
        isSubmitting={isSubmitting}
      />
    </>
  );
}