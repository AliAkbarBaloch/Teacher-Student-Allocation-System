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
  AllocationPlanDialogs,
  useAllocationPlansPage,
  useAllocationPlansColumnConfig,
} from "@/features/allocation-plans";
// types
import type { AllocationPlan } from "@/features/allocation-plans/types/allocationPlan.types";
// utils
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

interface AllocationPlanPageState {
  allocationPlanToDelete: AllocationPlan | null;
}

interface AllocationPlanActions {
  onView: (plan: AllocationPlan) => void;
  onEdit: (plan: AllocationPlan) => void;
  onDelete: (plan: AllocationPlan) => void;
}

export default function AllocationPlanPage() {
  const { t } = useTranslation("allocationPlans");
  const dialogs = useDialogState();
  const [state, setState] = useState<AllocationPlanPageState>({
    allocationPlanToDelete: null,
  });

  const {
    allocationPlans,
    loading,
    error,
    selectedAllocationPlan,
    setSelectedAllocationPlan,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useAllocationPlansPage();

  const columnConfig = useAllocationPlansColumnConfig();

  const resetDialogState = useCallback(() => {
    setSelectedAllocationPlan(null);
    setState(prev => ({ ...prev, allocationPlanToDelete: null }));
  }, [setSelectedAllocationPlan]);

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
      if (!selectedAllocationPlan) {
        return;
      }
      
      try {
        await handleUpdateInternal(data, selectedAllocationPlan.id);
        dialogs.edit.setIsOpen(false);
        resetDialogState();
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedAllocationPlan, dialogs.edit, resetDialogState]
  );

  const handleDelete = useCallback(async () => {
    if (!state.allocationPlanToDelete) {
      return;
    }
    
    try {
      await handleDeleteInternal(state.allocationPlanToDelete.id);
      dialogs.delete.setIsOpen(false);
      resetDialogState();
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, state.allocationPlanToDelete, dialogs.delete, resetDialogState]);

  const handleEditClick = useCallback(
    (allocationPlan: AllocationPlan) => {
      setSelectedAllocationPlan(allocationPlan);
      dialogs.edit.setIsOpen(true);
    },
    [setSelectedAllocationPlan, dialogs.edit]
  );

  const handleDeleteClick = useCallback(
    (allocationPlan: AllocationPlan) => {
      setState(prev => ({ ...prev, allocationPlanToDelete: allocationPlan }));
      dialogs.delete.setIsOpen(true);
    },
    [dialogs.delete]
  );

  const handleViewClick = useCallback(
    (allocationPlan: AllocationPlan) => {
      setSelectedAllocationPlan(allocationPlan);
      dialogs.view.setIsOpen(true);
    },
    [setSelectedAllocationPlan, dialogs.view]
  );

  const actions: AllocationPlanActions = {
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
          <h2 className="text-2xl font-semibold tracking-tight">
            {t("title")}
          </h2>
          <p className="text-muted-foreground text-sm mt-1">{t("subtitle")}</p>
        </div>
        <Button onClick={() => dialogs.create.setIsOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          {t("actions.create")}
        </Button>
      </div>

      <DataTable
        columnConfig={columnConfig}
        data={allocationPlans}
        searchKey="planName"
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

      <AllocationPlanDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedAllocationPlan={selectedAllocationPlan}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedAllocationPlan}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}