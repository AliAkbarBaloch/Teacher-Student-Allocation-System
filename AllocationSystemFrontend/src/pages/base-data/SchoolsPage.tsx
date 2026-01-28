import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";

import type {
  School,
  CreateSchoolRequest,
  UpdateSchoolRequest,
} from "@/features/schools/types/school.types";
import { SchoolDialogs } from "@/features/schools/components/SchoolDialogs";
import { SchoolFilters } from "@/features/schools/components/SchoolFilters";
import { SchoolsPageHeader } from "@/features/schools/components/SchoolsPageHeader";
import { useSchoolsPage } from "@/features/schools/hooks/useSchoolsPage";
import { useSchoolsColumnConfig } from "@/features/schools/utils/columnConfig";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useDialogState } from "@/hooks/useDialogState";
import { DataTable } from "@/components/common/DataTable";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { Power } from "lucide-react";

export default function SchoolsPage() {
  const { t } = useTranslation("schools");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const dialogs = useDialogState();
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);

  const {
    schools,
    selectedSchool,
    setSelectedSchool,
    error,
    loading,
    formLoading,
    isSearchInputLoading,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    searchInput,
    selectedType,
    zoneFilter,
    statusFilter,
    handleSearchChange,
    handleSchoolTypeChange,
    handleZoneChange,
    handleStatusFilterChange,
    handleResetFilters,
    fetchSchoolDetails,
    handleCreateSubmit: handleCreateSubmitInternal,
    handleUpdateSubmit: handleUpdateSubmitInternal,
    handleStatusChange,
    handleDelete,
    isSubmitting,
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
  } = useSchoolsPage();

  const columnConfig = useSchoolsColumnConfig();

  const handleOpenCreate = () => {
    setSelectedSchool(null);
    dialogs.create.setIsOpen(true);
  };

  const handleOpenView = useCallback((school: School) => {
    setSelectedSchool(school);
    dialogs.view.setIsOpen(true);
  }, [setSelectedSchool, dialogs.view]);

  const handleOpenEdit = useCallback(async (school: School) => {
    try {
      await fetchSchoolDetails(school.id);
      dialogs.edit.setIsOpen(true);
    } catch {
      // toast already handled
    }
  }, [fetchSchoolDetails, dialogs.edit]);

  const handleCreateSubmit = async (payload: CreateSchoolRequest) => {
    try {
      await handleCreateSubmitInternal(payload);
      dialogs.create.setIsOpen(false);
    } catch {
      // Error already handled
    }
  };

  const handleUpdateSubmit = async (payload: UpdateSchoolRequest) => {
    try {
      await handleUpdateSubmitInternal(payload);
      dialogs.edit.setIsOpen(false);
    } catch {
      // Error already handled
    }
  };

  const openStatusDialog = useCallback((school: School) => {
    setStatusTarget({ school, nextState: !school.isActive });
    setIsStatusDialogOpen(true);
  }, [setStatusTarget]);

  const openDeleteDialog = useCallback((school: School) => {
    setDeleteTarget(school);
    dialogs.delete.setIsOpen(true);
  }, [setDeleteTarget, dialogs.delete]);

  const confirmStatusChange = async () => {
    if (!statusTarget.school) {
      return;
    }
    try {
      await handleStatusChange(statusTarget.school, statusTarget.nextState);
      setIsStatusDialogOpen(false);
      setStatusTarget({ school: null, nextState: false });
    } catch {
      // Error already handled
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) {
      return;
    }
    try {
      await handleDelete(deleteTarget);
      dialogs.delete.setIsOpen(false);
      setDeleteTarget(null);
    } catch {
      // Error already handled
    }
  };

  return (
    <div className="space-y-6 w-full min-w-0 max-w-full">
      <SchoolsPageHeader
        isAdmin={isAdmin}
        onCreate={handleOpenCreate}
        title={t("title")}
        subtitle={t("subtitle")}
        createLabel={t("actions.create")}
        readOnlyTitle={t("permissions.readOnlyTitle")}
        readOnlyDescription={t("permissions.readOnlyDescription")}
      />

      <SchoolFilters
        searchValue={searchInput}
        onSearchChange={handleSearchChange}
        searchLoading={isSearchInputLoading}
        schoolType={selectedType}
        onSchoolTypeChange={handleSchoolTypeChange}
        zoneNumber={zoneFilter}
        onZoneNumberChange={handleZoneChange}
        status={statusFilter}
        onStatusChange={handleStatusFilterChange}
        onReset={handleResetFilters}
      />

      <DataTable
        columnConfig={columnConfig}
        data={schools}
        searchKey="schoolName"
        searchPlaceholder={t("filters.searchPlaceholder")}
        enableSearch={false}
        enableColumnVisibility={false}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.empty")}
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
          onView: handleOpenView,
          onEdit: isAdmin ? handleOpenEdit : undefined,
          onDelete: isAdmin ? openDeleteDialog : undefined,
          customActions: isAdmin
            ? [
                {
                  label: (school: School) =>
                    school.isActive ? t("actions.deactivate") : t("actions.activate"),
                  icon: <Power className="h-4 w-4" />,
                  onClick: openStatusDialog,
                  separator: false,
                },
              ]
            : undefined,
          labels: {
            view: t("actions.view"),
            edit: t("actions.edit"),
            delete: t("actions.delete"),
          },
        }}
      />

      <SchoolDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isStatusDialogOpen={isStatusDialogOpen}
        setIsStatusDialogOpen={setIsStatusDialogOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedSchool={selectedSchool}
        formLoading={formLoading}
        statusTarget={statusTarget}
        deleteTarget={deleteTarget}
        onCreateSubmit={handleCreateSubmit}
        onUpdateSubmit={handleUpdateSubmit}
        onStatusChange={confirmStatusChange}
        onDelete={confirmDelete}
        onOpenEdit={handleOpenEdit}
        onStatusTargetChange={setStatusTarget}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}

