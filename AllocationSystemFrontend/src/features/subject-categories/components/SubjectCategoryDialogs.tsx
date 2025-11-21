import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { SubjectCategoryForm } from "./SubjectCategoryForm";
import type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
} from "../types/subjectCategory.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface SubjectCategoryDialogsProps {
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
  selectedSubjectCategory: SubjectCategory | null;

  // Handlers
  onCreateSubmit: (data: CreateSubjectCategoryRequest | UpdateSubjectCategoryRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateSubjectCategoryRequest | UpdateSubjectCategoryRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (subjectCategory: SubjectCategory) => void;
  onSelectedChange: (subjectCategory: SubjectCategory | null) => void;

  // States
  isSubmitting: boolean;
  formError?: string | null;

  // Translations
  t: TFunction<"subjectCategories">;
}

export function SubjectCategoryDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedSubjectCategory,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  formError,
  t,
}: SubjectCategoryDialogsProps) {
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
          <SubjectCategoryForm
            onSubmit={onCreateSubmit}
            onCancel={() => setIsCreateDialogOpen(false)}
            isLoading={isSubmitting}
            error={formError}
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
        data={selectedSubjectCategory}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedSubjectCategory) {
            onEditClick(selectedSubjectCategory);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(subjectCategory) => (
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("form.fields.title")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {subjectCategory.categoryTitle}
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
          {selectedSubjectCategory && (
            <SubjectCategoryForm
              key={`edit-${selectedSubjectCategory.id}`}
              subjectCategory={selectedSubjectCategory}
              onSubmit={onUpdateSubmit}
              onCancel={() => {
                setIsEditDialogOpen(false);
                onSelectedChange(null);
              }}
              isLoading={isSubmitting}
              error={formError}
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

