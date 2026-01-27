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
// utils
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

/**
 * State interface for the InternshipTypesPage component
 * @interface InternshipTypesPageState
 */
interface InternshipTypesPageState {
  internshipTypeToDelete: InternshipType | null;
}

/**
 * Actions interface defining callback functions for internship type operations
 * @interface InternshipTypeActions
 */
interface InternshipTypeActions {
  onView: (type: InternshipType) => void;
  onEdit: (type: InternshipType) => void;
  onDelete: (type: InternshipType) => void;
}

export default function InternshipTypesPage() {
  const { t } = useTranslation("internshipTypes");
  const dialogs = useDialogState();
  const [state, setState] = useState<InternshipTypesPageState>({
    internshipTypeToDelete: null,
  });

  const {
    internshipTypes,
    loading,
    error,
    selectedInternshipType,
    setSelectedInternshipType,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useInternshipTypesPage();

  const columnConfig = useInternshipTypesColumnConfig();

  const resetDialogState = useCallback(() => {
    setSelectedInternshipType(null);
    setState(prev => ({ ...prev, internshipTypeToDelete: null }));
  }, [setSelectedInternshipType]);

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
      if (!selectedInternshipType) {
        return;
      }
      
      try {
        await handleUpdateInternal(data, selectedInternshipType.id);
        dialogs.edit.setIsOpen(false);
        resetDialogState();
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedInternshipType, dialogs.edit, resetDialogState]
  );

  const handleDelete = useCallback(async () => {
    if (!state.internshipTypeToDelete) {
      return;
    }
    
    try {
      await handleDeleteInternal(state.internshipTypeToDelete.id);
      dialogs.delete.setIsOpen(false);
      resetDialogState();
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, state.internshipTypeToDelete, dialogs.delete, resetDialogState]);

  const handleEditClick = useCallback((internshipType: InternshipType) => {
    setSelectedInternshipType(internshipType);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedInternshipType, dialogs.edit]);

  const handleDeleteClick = useCallback((internshipType: InternshipType) => {
    setState(prev => ({ ...prev, internshipTypeToDelete: internshipType }));
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((internshipType: InternshipType) => {
    setSelectedInternshipType(internshipType);
    dialogs.view.setIsOpen(true);
  }, [setSelectedInternshipType, dialogs.view]);

  const actions: InternshipTypeActions = {
    onView: handleViewClick,
    onEdit: handleEditClick,
    onDelete: handleDeleteClick,
  };

  const actionLabels = {
    view: t("actions.view"),
    edit: t("actions.edit"),
    delete: t("actions.delete"),
  };

  const serverPagination = {
    page: pagination.page,
    pageSize: pagination.pageSize,
    totalItems: pagination.totalItems,
    totalPages: pagination.totalPages,
    onPageChange: handlePageChange,
    onPageSizeChange: handlePageSizeChange,
  };

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
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        pageSizeOptions={[...TABLE_PAGE_SIZE_OPTIONS]}
        serverSidePagination={serverPagination}
        actions={{
          ...actions,
          labels: actionLabels,
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