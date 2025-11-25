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
import {
  AcademicYearDialogs,
  useAcademicYearsPage,
  useAcademicYearsColumnConfig,
} from "@/features/academic-years";
// types
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";

export default function AcademicYearPage() {
  const { t } = useTranslation("academicYears");
  const dialogs = useDialogState();
  const [academicYearToDelete, setAcademicYearToDelete] = useState<AcademicYear | null>(null);

  const {
    academicYears,
    loading,
    error,
    selectedAcademicYear,
    setSelectedAcademicYear,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useAcademicYearsPage();

  const columnConfig = useAcademicYearsColumnConfig();

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
      if (!selectedAcademicYear) return;
      try {
        await handleUpdateInternal(data, selectedAcademicYear.id);
        dialogs.edit.setIsOpen(false);
        setSelectedAcademicYear(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedAcademicYear, setSelectedAcademicYear, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!academicYearToDelete) return;
    try {
      await handleDeleteInternal(academicYearToDelete.id);
      dialogs.delete.setIsOpen(false);
      setAcademicYearToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, academicYearToDelete, dialogs.delete]);

  const handleEditClick = useCallback((academicYear: AcademicYear) => {
    setSelectedAcademicYear(academicYear);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedAcademicYear, dialogs.edit]);

  const handleDeleteClick = useCallback((academicYear: AcademicYear) => {
    setAcademicYearToDelete(academicYear);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((academicYear: AcademicYear) => {
    setSelectedAcademicYear(academicYear);
    dialogs.view.setIsOpen(true);
  }, [setSelectedAcademicYear, dialogs.view]);

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
        data={academicYears}
        searchKey="yearName"
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

      <AcademicYearDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedAcademicYear={selectedAcademicYear}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedAcademicYear}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}