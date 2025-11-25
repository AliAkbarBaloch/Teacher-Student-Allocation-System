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
  TeacherSubjectDialogs,
  useTeacherSubjectsPage,
  useTeacherSubjectsColumnConfig,
} from "@/features/teacher-subjects";
// types
import type { TeacherSubject } from "@/features/teacher-subjects/types/teacherSubject.types";

export default function TeacherSubjectsPage() {
  const { t } = useTranslation("teacherSubjects");
  const dialogs = useDialogState();
  const [teacherSubjectToDelete, setTeacherSubjectToDelete] = useState<TeacherSubject | null>(null);

  const {
    teacherSubjects,
    loading,
    error,
    selectedTeacherSubject,
    setSelectedTeacherSubject,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useTeacherSubjectsPage();

  const columnConfig = useTeacherSubjectsColumnConfig();

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
      if (!selectedTeacherSubject) return;
      try {
        await handleUpdateInternal(data, selectedTeacherSubject.id);
        dialogs.edit.setIsOpen(false);
        setSelectedTeacherSubject(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedTeacherSubject, setSelectedTeacherSubject, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!teacherSubjectToDelete) return;
    try {
      await handleDeleteInternal(teacherSubjectToDelete.id);
      dialogs.delete.setIsOpen(false);
      setTeacherSubjectToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, teacherSubjectToDelete, dialogs.delete]);

  const handleEditClick = useCallback((teacherSubject: TeacherSubject) => {
    setSelectedTeacherSubject(teacherSubject);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedTeacherSubject, dialogs.edit]);

  const handleDeleteClick = useCallback((teacherSubject: TeacherSubject) => {
    setTeacherSubjectToDelete(teacherSubject);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((teacherSubject: TeacherSubject) => {
    setSelectedTeacherSubject(teacherSubject);
    dialogs.view.setIsOpen(true);
  }, [setSelectedTeacherSubject, dialogs.view]);

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
        data={teacherSubjects}
        searchKey="subjectTitle"
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

      <TeacherSubjectDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedTeacherSubject={selectedTeacherSubject}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedTeacherSubject}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}