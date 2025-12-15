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
import { TeacherAssignmentForm } from "./TeacherAssignmentForm";
import { ReadOnlyField } from "@/components/form/view/ReadOnlyField";
import { Badge } from "@/components/ui/badge";

// types
import type {
  TeacherAssignment,
  CreateTeacherAssignmentRequest,
  UpdateTeacherAssignmentRequest,
} from "../types/teacherAssignment.types";

// hooks
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

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
  onCreateSubmit: (
    data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest
  ) => Promise<void>;
  onUpdateSubmit: (
    data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest
  ) => Promise<void>;
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
                <ReadOnlyField
                  label={t("form.fields.planId")}
                  value={assignment.planTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.teacherId")}
                  value={assignment.teacherTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.internshipTypeId")}
                  value={assignment.internshipTypeTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.subjectId")}
                  value={assignment.subjectTitle}
                />
                <ReadOnlyField
                  label={t("form.fields.studentGroupSize")}
                  value={assignment.studentGroupSize}
                />
                <ReadOnlyField
                  label={t("form.fields.assignmentStatus")}
                  value={
                    <Badge
                      variant={
                        assignment.assignmentStatus === "CONFIRMED"
                          ? "success"
                          : assignment.assignmentStatus === "CANCELLED"
                          ? "secondary"
                          : assignment.assignmentStatus === "ON_HOLD"
                          ? "muted"
                          : "default"
                      }
                      className="rounded-sm"
                    >
                      {t(
                        `form.status.${assignment.assignmentStatus.toLowerCase()}`
                      )}
                    </Badge>
                  }
                />
                <ReadOnlyField
                  label={t("form.fields.isManualOverride")}
                  value={
                    <Badge
                      variant={
                        assignment.isManualOverride ? "success" : "muted"
                      }
                      className="rounded-sm"
                    >
                      {assignment.isManualOverride
                        ? t("table.yes")
                        : t("table.no")}
                    </Badge>
                  }
                />

                <ReadOnlyField
                  label={t("form.fields.notes")}
                  value={assignment.notes || "-"}
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
