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
import { TeacherAssignmentForm } from "./TeacherAssignmentForm";
import type {
  TeacherAssignment,
  CreateTeacherAssignmentRequest,
  UpdateTeacherAssignmentRequest,
} from "../types/teacherAssignment.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface TeacherAssignmentDialogsProps {
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
  selectedAssignment: TeacherAssignment | null;

  // Handlers
  onCreateSubmit: (data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (assignment: TeacherAssignment) => void;
  onSelectedChange: (assignment: TeacherAssignment | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"teacher-assignments">;
}

export function TeacherAssignmentDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedAssignment,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: TeacherAssignmentDialogsProps) {
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
            <TeacherAssignmentForm
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
        data={selectedAssignment}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedAssignment) {
            onEditClick(selectedAssignment);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(assignment) => (
          <DialogBody>
            <div className="space-y-4 py-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.planId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.planId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.teacherId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.teacherId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.internshipTypeId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.internshipTypeId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.subjectId")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.subjectId}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.studentGroupSize")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.studentGroupSize}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.assignmentStatus")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {t(`form.status.${assignment.assignmentStatus.toLowerCase()}`)}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.isManualOverride")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.isManualOverride ? t("table.yes") : t("table.no")}
                  </div>
                </div>
                <div className="space-y-2 md:col-span-2">
                  <label className="text-sm font-medium">{t("form.fields.notes")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {assignment.notes || "-"}
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
            {selectedAssignment && (
              <TeacherAssignmentForm
                key={`edit-${selectedAssignment.id}`}
                assignment={selectedAssignment}
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