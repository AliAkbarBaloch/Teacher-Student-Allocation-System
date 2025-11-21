// react
import { useState, useCallback } from "react";
// translation
import { useTranslation } from "react-i18next";
// icon
import { Plus } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/common/DataTable";
// hooks
import { useDialogState } from "@/hooks/useDialogState";
// features
import { InternshipTypeDialogs } from "@/features/internship-types/components/InternshipTypeDialogs";
import { useInternshipTypesPage } from "@/features/internship-types/hooks/useInternshipTypesPage";
import { useInternshipTypesColumnConfig } from "@/features/internship-types/utils/columnConfig";
// types
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";

export default function InternshipTypesPage() {
  const { t } = useTranslation("internshipTypes");
  const dialogs = useDialogState();
  const [internshipTypeToDelete, setInternshipTypeToDelete] = useState<InternshipType | null>(null);

  const {
    internshipTypes,
    loading,
    error,
    selectedInternshipType,
    setSelectedInternshipType,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useInternshipTypesPage();

  const columnConfig = useInternshipTypesColumnConfig();

  const handleCreate = useCallback(
    async (data: Parameters<typeof handleCreateInternal>[0]) => {
      try {
        await handleCreateInternal(data);
        dialogs.create.setIsOpen(false);
      } catch {
        // Error already handled in hook
      }
    },
    [handleCreateInternal, dialogs.create]
  );

  const handleUpdate = useCallback(
    async (data: Parameters<typeof handleUpdateInternal>[0]) => {
      if (!selectedInternshipType) return;
      try {
        await handleUpdateInternal(data, selectedInternshipType.id);
        dialogs.edit.setIsOpen(false);
        setSelectedInternshipType(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedInternshipType, setSelectedInternshipType, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!internshipTypeToDelete) return;
    try {
      await handleDeleteInternal(internshipTypeToDelete.id);
      dialogs.delete.setIsOpen(false);
      setInternshipTypeToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, internshipTypeToDelete, dialogs.delete]);

  const handleEditClick = useCallback((internshipType: InternshipType) => {
    setSelectedInternshipType(internshipType);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedInternshipType, dialogs.edit]);

  const handleDeleteClick = useCallback((internshipType: InternshipType) => {
    setInternshipTypeToDelete(internshipType);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((internshipType: InternshipType) => {
    setSelectedInternshipType(internshipType);
    dialogs.view.setIsOpen(true);
  }, [setSelectedInternshipType, dialogs.view]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">{t("title")}</h2>
          <p className="text-muted-foreground text-sm mt-1">{t("subtitle")}</p>
        </div>
        <Button onClick={() => dialogs.create.setIsOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          {t("actions.create")}
        </Button>
      </div>

      <DataTable
        columnConfig={columnConfig}
        data={internshipTypes}
        searchKey="fullName"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={true}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        actions={{
          onView: handleViewClick,
          onEdit: handleEditClick,
          onDelete: handleDeleteClick,
          labels: {
            view: t("actions.view"),
            edit: t("actions.edit"),
            delete: t("actions.delete"),
          },
        }}
      />

      <InternshipTypeDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedInternshipType={selectedInternshipType}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedInternshipType}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}

