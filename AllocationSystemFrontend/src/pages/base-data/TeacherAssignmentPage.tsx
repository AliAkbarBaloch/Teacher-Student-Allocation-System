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
  TeacherAssignmentDialogs,
  useTeacherAssignmentsPage,
  useTeacherAssignmentsColumnConfig,
} from "@/features/teacher-assignments";
// types
import type { TeacherAssignment} from "@/features/teacher-assignments/types/teacherAssignment.types";

export default function TeacherAssignmentPage() {
  const { t } = useTranslation("teacherAssignments");
  const dialogs = useDialogState();
  const [assignmentToDelete, setAssignmentToDelete] = useState<TeacherAssignment | null>(null);

  const {
    teacherAssignments,
    loading,
    error,
    selectedAssignment,
    setSelectedAssignment,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useTeacherAssignmentsPage();

  const columnConfig = useTeacherAssignmentsColumnConfig();

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
      if (!selectedAssignment) return;
      try {
        await handleUpdateInternal(data, selectedAssignment.id);
        dialogs.edit.setIsOpen(false);
        setSelectedAssignment(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedAssignment, setSelectedAssignment, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!assignmentToDelete) return;
    try {
      await handleDeleteInternal(assignmentToDelete.id);
      dialogs.delete.setIsOpen(false);
      setAssignmentToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, assignmentToDelete, dialogs.delete]);

  const handleEditClick = useCallback((assignment: TeacherAssignment) => {
    setSelectedAssignment(assignment);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedAssignment, dialogs.edit]);

  const handleDeleteClick = useCallback((assignment: TeacherAssignment) => {
    setAssignmentToDelete(assignment);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((assignment: TeacherAssignment) => {
    setSelectedAssignment(assignment);
    dialogs.view.setIsOpen(true);
  }, [setSelectedAssignment, dialogs.view]);

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
        data={teacherAssignments}
        searchKey="planId"
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

      <TeacherAssignmentDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedAssignment={selectedAssignment}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedAssignment}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}