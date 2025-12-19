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
  PlanChangeLogsDialogs,
  usePlanChangeLogsPage,
  usePlanChangeLogsColumnConfig,
} from "@/features/plan-change-logs";
// types
import type { PlanChangeLog } from "@/features/plan-change-logs/types/planChangeLog.types";
// utils
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

export default function PlanChangeLogsPage() {
  const { t } = useTranslation("planChangeLogs");
  const dialogs = useDialogState();
  const [planChangeLogToDelete, setPlanChangeLogToDelete] = useState<PlanChangeLog | null>(null);

  const {
    planChangeLogs,
    loading,
    error,
    selectedPlanChangeLog,
    setSelectedPlanChangeLog,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = usePlanChangeLogsPage();

  const columnConfig = usePlanChangeLogsColumnConfig();

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
      if (!selectedPlanChangeLog) return;
      try {
        await handleUpdateInternal(data, selectedPlanChangeLog.id);
        dialogs.edit.setIsOpen(false);
        setSelectedPlanChangeLog(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedPlanChangeLog, setSelectedPlanChangeLog, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!planChangeLogToDelete) return;
    try {
      await handleDeleteInternal(planChangeLogToDelete.id);
      dialogs.delete.setIsOpen(false);
      setPlanChangeLogToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, planChangeLogToDelete, dialogs.delete]);

  const handleEditClick = useCallback((log: PlanChangeLog) => {
    setSelectedPlanChangeLog(log);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedPlanChangeLog, dialogs.edit]);

  const handleDeleteClick = useCallback((log: PlanChangeLog) => {
    setPlanChangeLogToDelete(log);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((log: PlanChangeLog) => {
    setSelectedPlanChangeLog(log);
    dialogs.view.setIsOpen(true);
  }, [setSelectedPlanChangeLog, dialogs.view]);

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
        data={planChangeLogs}
        searchKey="changeType"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        pageSizeOptions={[...TABLE_PAGE_SIZE_OPTIONS]}
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

      <PlanChangeLogsDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedPlanChangeLog={selectedPlanChangeLog}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedPlanChangeLog}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}