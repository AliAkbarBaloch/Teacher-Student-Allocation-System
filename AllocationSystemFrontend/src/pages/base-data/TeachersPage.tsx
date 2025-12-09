import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useTeachersPage } from "@/features/teachers/hooks/useTeachersPage";
import { TeacherDialogs } from "@/features/teachers/components/TeacherDialogs";
import { TeacherFilters } from "@/features/teachers/components/TeacherFilters";
import { TeachersPageHeader } from "@/features/teachers/components/TeachersPageHeader";
import { BulkImportDialog } from "@/features/teachers/components/BulkImportDialog";
import { DataTable } from "@/components/common/DataTable";
import { useTeachersColumnConfig } from "@/features/teachers/utils/columnConfig";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useDialogState } from "@/hooks/useDialogState";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { Power } from "lucide-react";
import type { Teacher, CreateTeacherRequest, UpdateTeacherRequest } from "@/features/teachers/types/teacher.types";

export default function TeachersPage() {
  const { t } = useTranslation("teachers");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const dialogs = useDialogState();
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [isBulkImportDialogOpen, setIsBulkImportDialogOpen] = useState(false);

  const {
    teachers,
    selectedTeacher,
    setSelectedTeacher,
    error,
    loading,
    formLoading,
    isSearchInputLoading,
    createFormKey,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    searchInput,
    selectedSchoolId,
    selectedEmploymentStatus,
    statusFilter,
    handleSearchChange,
    handleSchoolIdChange,
    handleEmploymentStatusChange,
    handleStatusFilterChange,
    handleResetFilters,
    fetchTeacherDetails,
    handleCreateSubmit: handleCreateSubmitInternal,
    handleUpdateSubmit: handleUpdateSubmitInternal,
    handleStatusChange,
    handleDelete,
    handleOpenCreate,
    refreshList,
    isSubmitting,
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
    warningMessage,
    setWarningMessage,
  } = useTeachersPage();

  const handleOpenView = (teacher: Teacher) => {
    setSelectedTeacher(teacher);
    dialogs.view.setIsOpen(true);
  };

  const handleOpenEdit = async (teacher: Teacher) => {
      await fetchTeacherDetails(teacher.id);
      dialogs.edit.setIsOpen(true);

  };

  const handleEdit = async () => {
    if (!selectedTeacher) return;
    dialogs.view.setIsOpen(false);
    await handleOpenEdit(selectedTeacher);
  };

  const handleCreateSubmit = async (payload: CreateTeacherRequest) => {
      await handleCreateSubmitInternal(payload);
      dialogs.create.setIsOpen(false);
  };

  const handleUpdateSubmit = async (payload: UpdateTeacherRequest) => {
      await handleUpdateSubmitInternal(payload);
      // Success toast is shown in the hook
      dialogs.edit.setIsOpen(false);
    
  };

  const openStatusDialog = (teacher: Teacher) => {
    setStatusTarget({ teacher, nextState: !teacher.isActive });
    setWarningMessage(null);
    setIsStatusDialogOpen(true);
  };

  const openDeleteDialog = (teacher: Teacher) => {
    setDeleteTarget(teacher);
    dialogs.delete.setIsOpen(true);
  };

  const confirmStatusChange = async () => {
    if (!statusTarget.teacher) return;
      await handleStatusChange(statusTarget.teacher, statusTarget.nextState);
      setIsStatusDialogOpen(false);
      setStatusTarget({ teacher: null, nextState: false });
      setWarningMessage(null);
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
      await handleDelete(deleteTarget);
      // Success toast is shown in the hook
      dialogs.delete.setIsOpen(false);
      setDeleteTarget(null);
  };

  const handleOpenCreateWithDialog = () => {
    handleOpenCreate();
    dialogs.create.setIsOpen(true);
  };

  const handleCloseStatus = () => {
    setStatusTarget({ teacher: null, nextState: false });
    setWarningMessage(null);
  };

  const columnConfig = useTeachersColumnConfig();

  return (
    <div className="space-y-6">
      <TeachersPageHeader
        isAdmin={isAdmin}
        onCreate={handleOpenCreateWithDialog}
        onBulkImport={() => setIsBulkImportDialogOpen(true)}
        title={t("title")}
        subtitle={t("subtitle")}
        createLabel={t("actions.create")}
        readOnlyTitle={t("permissions.readOnlyTitle")}
        readOnlyDescription={t("permissions.readOnlyDescription")}
      />

      <TeacherFilters
        searchValue={searchInput}
        onSearchChange={handleSearchChange}
        searchLoading={isSearchInputLoading}
        schoolId={selectedSchoolId}
        onSchoolIdChange={handleSchoolIdChange}
        employmentStatus={selectedEmploymentStatus}
        onEmploymentStatusChange={handleEmploymentStatusChange}
        status={statusFilter}
        onStatusChange={handleStatusFilterChange}
        onReset={handleResetFilters}
      />

      <DataTable
        columnConfig={columnConfig}
        data={teachers}
        searchKey="email"
        searchPlaceholder={t("filters.searchPlaceholder")}
        enableSearch={false}
        enableColumnVisibility={false}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.empty")}
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
          onView: handleOpenView,
          onEdit: isAdmin ? handleOpenEdit : undefined,
          onDelete: isAdmin ? openDeleteDialog : undefined,
          customActions: isAdmin
            ? [
                {
                  label: (teacher: Teacher) =>
                    teacher.isActive ? t("actions.deactivate") : t("actions.activate"),
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

      <TeacherDialogs
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
        selectedTeacher={selectedTeacher}
        formLoading={formLoading}
        createFormKey={createFormKey}
        statusTarget={statusTarget}
        deleteTarget={deleteTarget}
        warningMessage={warningMessage}
        onCreateSubmit={handleCreateSubmit}
        onUpdateSubmit={handleUpdateSubmit}
        onStatusChange={confirmStatusChange}
        onDelete={confirmDelete}
        onCloseStatus={handleCloseStatus}
        onEdit={handleEdit}
        isSubmitting={isSubmitting}
        isAdmin={isAdmin}
        t={t}
      />

      {isAdmin && (
        <BulkImportDialog
          open={isBulkImportDialogOpen}
          onOpenChange={setIsBulkImportDialogOpen}
          onImportComplete={async () => {
            // Refresh the teacher list after successful import
            await refreshList();
          }}
        />
      )}
    </div>
  );
}

