import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { GenericFormDialog } from "@/components/common/GenericFormDialog";
import { GenericForm } from "@/components/common/GenericForm";
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
} from "../types/subject.types";
import { getSubjectFieldConfig } from "../config/subjectFieldConfig";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useMemo } from "react";

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
  
  // Get field configuration (memoized to avoid recreating on every render)
  const fieldConfig = useMemo(() => getSubjectFieldConfig(t), [t]);

  return (
    <>
      {/* Create Dialog */}
      <GenericFormDialog
        open={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        title={t("form.title.create")}
        description={t("subtitle")}
        maxWidth="2xl"
      >
        <GenericForm<Subject, CreateSubjectRequest, UpdateSubjectRequest>
          fields={fieldConfig}
          initialData={null}
          onSubmit={onCreateSubmit}
          onCancel={() => setIsCreateDialogOpen(false)}
          isLoading={isSubmitting}
          mode="create"
          translationNamespace="subjects"
        />
      </GenericFormDialog>

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
        fieldConfig={fieldConfig}
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedSubject) {
            onEditClick(selectedSubject);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
      />

      {/* Edit Dialog */}
      <GenericFormDialog
        open={isEditDialogOpen}
        onOpenChange={setIsEditDialogOpen}
        title={t("form.title.edit")}
        description={t("subtitle")}
        maxWidth="2xl"
      >
        {selectedSubject && (
          <GenericForm<Subject, CreateSubjectRequest, UpdateSubjectRequest>
            key={`edit-${selectedSubject.id}`}
            fields={fieldConfig}
            initialData={selectedSubject}
            onSubmit={onUpdateSubmit}
            onCancel={() => {
              setIsEditDialogOpen(false);
              onSelectedChange(null);
            }}
            isLoading={isSubmitting}
            mode="edit"
            translationNamespace="subjects"
          />
        )}
      </GenericFormDialog>

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

