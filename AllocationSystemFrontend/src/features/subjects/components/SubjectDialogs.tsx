import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { SubjectForm } from "./SubjectForm";
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
} from "../types/subject.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface SubjectDialogsProps {
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
  selectedSubject: Subject | null;

  // Handlers
  onCreateSubmit: (data: CreateSubjectRequest | UpdateSubjectRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateSubjectRequest | UpdateSubjectRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (subject: Subject) => void;
  onSelectedChange: (subject: Subject | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"subjects">;
}

export function SubjectDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedSubject,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: SubjectDialogsProps) {
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
          <SubjectForm
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
        data={selectedSubject}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedSubject) {
            onEditClick(selectedSubject);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(subject) => (
          <div className="space-y-4 py-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.code")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {subject.subjectCode}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.title")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {subject.subjectTitle}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.category")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {subject.subjectCategoryTitle || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.schoolType")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {subject.schoolType || "-"}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.isActive")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {subject.isActive ? t("table.active") : t("table.inactive")}
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
          {selectedSubject && (
            <SubjectForm
              key={`edit-${selectedSubject.id}`}
              subject={selectedSubject}
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

