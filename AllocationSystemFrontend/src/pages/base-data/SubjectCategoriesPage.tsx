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
import { SubjectCategoryDialogs } from "@/features/subject-categories/components/SubjectCategoryDialogs";
import { useSubjectCategoriesPage } from "@/features/subject-categories/hooks/useSubjectCategoriesPage";
import { useSubjectCategoriesColumnConfig } from "@/features/subject-categories/utils/columnConfig";
// types
import type { SubjectCategory } from "@/features/subject-categories/types/subjectCategory.types";
// utils
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

export default function SubjectCategoriesPage() {
  const { t } = useTranslation("subjectCategories");
  const dialogs = useDialogState();
  const [subjectCategoryToDelete, setSubjectCategoryToDelete] = useState<SubjectCategory | null>(null);

  const {
    subjectCategories,
    loading,
    error,
    selectedSubjectCategory,
    setSelectedSubjectCategory,
    isSubmitting,
    formError,
    setFormError,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useSubjectCategoriesPage();

  const columnConfig = useSubjectCategoriesColumnConfig();

  const handleCreate = useCallback(
    async (data: Parameters<typeof handleCreateInternal>[0]) => {
      try {
        await handleCreateInternal(data);
        dialogs.create.setIsOpen(false);
        setFormError(null);
      } catch {
        // Error already handled in hook and displayed in form
      }
    },
    [handleCreateInternal, dialogs.create, setFormError]
  );

  const handleUpdate = useCallback(
    async (data: Parameters<typeof handleUpdateInternal>[0]) => {
      if (!selectedSubjectCategory) {
        return;
      }
      try {
        await handleUpdateInternal(data, selectedSubjectCategory.id);
        dialogs.edit.setIsOpen(false);
        setSelectedSubjectCategory(null);
        setFormError(null);
      } catch {
        // Error already handled in hook and displayed in form
      }
    },
    [handleUpdateInternal, selectedSubjectCategory, setSelectedSubjectCategory, dialogs.edit, setFormError]
  );

  const handleDelete = useCallback(async () => {
    if (!subjectCategoryToDelete) {
      return;
    }
    try {
      await handleDeleteInternal(subjectCategoryToDelete.id);
      dialogs.delete.setIsOpen(false);
      setSubjectCategoryToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, subjectCategoryToDelete, dialogs.delete]);

  const handleEditClick = useCallback((subjectCategory: SubjectCategory) => {
    setSelectedSubjectCategory(subjectCategory);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedSubjectCategory, dialogs.edit]);

  const handleDeleteClick = useCallback((subjectCategory: SubjectCategory) => {
    setSubjectCategoryToDelete(subjectCategory);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((subjectCategory: SubjectCategory) => {
    setSelectedSubjectCategory(subjectCategory);
    dialogs.view.setIsOpen(true);
  }, [setSelectedSubjectCategory, dialogs.view]);

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
        data={subjectCategories}
        searchKey="categoryTitle"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        tableLayout="fixed"
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

      <SubjectCategoryDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={(open) => {
          dialogs.create.setIsOpen(open);
          if (!open) setFormError(null);
        }}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={(open) => {
          dialogs.edit.setIsOpen(open);
          if (!open) setFormError(null);
        }}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedSubjectCategory={selectedSubjectCategory}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedSubjectCategory}
        isSubmitting={isSubmitting}
        formError={formError}
        t={t}
      />
    </div>
  );
}

