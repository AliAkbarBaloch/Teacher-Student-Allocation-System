import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { TeacherAvailabilityForm } from "./TeacherAvailabilityForm";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
} from "../types/teacherAvailability.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

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
  onCreateSubmit: (data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest) => Promise<void>;
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
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <TeacherAvailabilityForm
            onSubmit={(data) => onCreateSubmit(data as CreateTeacherAvailabilityRequest)}
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
          <div className="space-y-4 py-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.teacher")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.teacherName}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.academicYear")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.academicYearName}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.internshipType")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.internshipTypeName}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.isAvailable")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.isAvailable ? t("table.available") : t("table.notAvailable")}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.preferenceRank")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.preferenceRank ?? "-"}
                </div>
              </div>
              <div className="space-y-2 md:col-span-2">
                <label className="text-sm font-medium">{t("form.fields.notes")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.notes || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.createdAt")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.createdAt
                    ? new Date(availability.createdAt).toLocaleString()
                    : "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.updatedAt")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {availability.updatedAt
                    ? new Date(availability.updatedAt).toLocaleString()
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
          {selectedTeacherAvailability && (
            <TeacherAvailabilityForm
              key={`edit-${selectedTeacherAvailability.availabilityId}`}
              teacherAvailability={selectedTeacherAvailability}
              onSubmit={(data) => onUpdateSubmit(data as UpdateTeacherAvailabilityRequest)}
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