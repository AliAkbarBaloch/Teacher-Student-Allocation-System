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
  TeacherAvailabilityDialogs,
  useTeacherAvailabilityPage,
  useTeacherAvailabilityColumnConfig,
} from "@/features/teacher-availability";
// types
import type { TeacherAvailability } from "@/features/teacher-availability/types/teacherAvailability.types";

export default function TeacherAvailabilityPage() {
  const { t } = useTranslation("teacherAvailability");
  const dialogs = useDialogState();
  const [availabilityToDelete, setAvailabilityToDelete] = useState<TeacherAvailability | null>(null);

  const {
    teacherAvailabilities,
    loading,
    error,
    selectedTeacherAvailability,
    setSelectedTeacherAvailability,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useTeacherAvailabilityPage();

  const columnConfig = useTeacherAvailabilityColumnConfig();

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
      if (!selectedTeacherAvailability) return;
      try {
        await handleUpdateInternal(data, selectedTeacherAvailability.id);
        dialogs.edit.setIsOpen(false);
        setSelectedTeacherAvailability(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedTeacherAvailability, setSelectedTeacherAvailability, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!availabilityToDelete) return;
    try {
      await handleDeleteInternal(availabilityToDelete.id);
      dialogs.delete.setIsOpen(false);
      setAvailabilityToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, availabilityToDelete, dialogs.delete]);

  const handleEditClick = useCallback((availability: TeacherAvailability) => {
    setSelectedTeacherAvailability(availability);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedTeacherAvailability, dialogs.edit]);

  const handleDeleteClick = useCallback((availability: TeacherAvailability) => {
    setAvailabilityToDelete(availability);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((availability: TeacherAvailability) => {
    setSelectedTeacherAvailability(availability);
    dialogs.view.setIsOpen(true);
  }, [setSelectedTeacherAvailability, dialogs.view]);

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
        data={teacherAvailabilities}
        searchKey="teacherFirstName"
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

      <TeacherAvailabilityDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedTeacherAvailability={selectedTeacherAvailability}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedTeacherAvailability}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}