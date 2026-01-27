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
import { SubjectDialogs } from "@/features/subjects/components/SubjectDialogs";
import { useSubjectsPage } from "@/features/subjects/hooks/useSubjectsPage";
import { useSubjectsColumnConfig } from "@/features/subjects/utils/columnConfig";
// types
import type { Subject } from "@/features/subjects/types/subject.types";
// utils
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

export default function SubjectsPage() {
  const { t } = useTranslation("subjects");
  const dialogs = useDialogState();
  const [subjectToDelete, setSubjectToDelete] = useState<Subject | null>(null);

  const {
    subjects,
    loading,
    error,
    selectedSubject,
    setSelectedSubject,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useSubjectsPage();

  const columnConfig = useSubjectsColumnConfig();

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
      if (!selectedSubject) {
        return;
      }
      try {
        await handleUpdateInternal(data, selectedSubject.id);
        dialogs.edit.setIsOpen(false);
        setSelectedSubject(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedSubject, setSelectedSubject, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!subjectToDelete) {
      return;
    }
    try {
      await handleDeleteInternal(subjectToDelete.id);
      dialogs.delete.setIsOpen(false);
      setSubjectToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, subjectToDelete, dialogs.delete]);

  const handleEditClick = useCallback((subject: Subject) => {
    setSelectedSubject(subject);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedSubject, dialogs.edit]);

  const handleDeleteClick = useCallback((subject: Subject) => {
    setSubjectToDelete(subject);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((subject: Subject) => {
    setSelectedSubject(subject);
    dialogs.view.setIsOpen(true);
  }, [setSelectedSubject, dialogs.view]);

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
        data={subjects}
        searchKey="subjectTitle"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        pageSizeOptions={TABLE_PAGE_SIZE_OPTIONS}
        serverSidePagination={{
          page: pagination.page,
          pageSize: pagination.pageSize,
          totalItems: pagination.totalItems,
          totalPages: pagination.totalPages,
          onPageChange: handlePageChange,
          onPageSizeChange: handlePageSizeChange,
        }}
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

      <SubjectDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedSubject={selectedSubject}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedSubject}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}

