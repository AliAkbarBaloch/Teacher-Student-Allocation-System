import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { InternshipTypeForm } from "./InternshipTypeForm";
import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
} from "../types/internshipType.types";
import type { TFunction } from "i18next";

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
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <InternshipTypeForm
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
        editLabel={t("actions.edit")}
        closeLabel={t("form.actions.close")}
        renderCustomContent={(internshipType) => (
          <div className="space-y-4 py-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.code")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.internshipCode}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.fullName")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.fullName}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.timing")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.timing || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.periodType")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.periodType || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.semester")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.semester || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.priorityOrder")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {internshipType.priorityOrder ?? "-"}
                </div>
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("form.fields.isSubjectSpecific")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {internshipType.isSubjectSpecific ? t("table.yes") : t("table.no")}
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

