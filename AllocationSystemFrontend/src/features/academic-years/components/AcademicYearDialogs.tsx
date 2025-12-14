import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { GenericForm } from "@/components/common/GenericForm";
import { GenericFormDialog } from "@/components/common/GenericFormDialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import type { TFunction } from "i18next";
import { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { getAcademicYearFieldConfig } from "../config/academicYearFieldConfig";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";

interface AcademicYearDialogsProps {
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
  selectedAcademicYear: AcademicYear | null;

  // Handlers
  onCreateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (academicYear: AcademicYear) => void;
  onSelectedChange: (academicYear: AcademicYear | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"academicYears">;
}

export function AcademicYearDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedAcademicYear,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: AcademicYearDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  
  // Get field configuration (memoized to avoid recreating on every render)
  const fieldConfig = useMemo(() => getAcademicYearFieldConfig(t), [t]);

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
        <GenericForm<AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest>
          fields={fieldConfig}
          initialData={null}
          onSubmit={onCreateSubmit}
          onCancel={() => setIsCreateDialogOpen(false)}
          isLoading={isSubmitting}
          mode="create"
          translationNamespace="academicYears"
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
        data={selectedAcademicYear}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        fieldConfig={fieldConfig}
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedAcademicYear) {
            onEditClick(selectedAcademicYear);
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
        {selectedAcademicYear && (
          <GenericForm<AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest>
            key={`edit-${selectedAcademicYear.id}`}
            fields={fieldConfig}
            initialData={selectedAcademicYear}
            onSubmit={onUpdateSubmit}
            onCancel={() => {
              setIsEditDialogOpen(false);
              onSelectedChange(null);
            }}
            isLoading={isSubmitting}
            mode="edit"
            translationNamespace="academicYears"
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