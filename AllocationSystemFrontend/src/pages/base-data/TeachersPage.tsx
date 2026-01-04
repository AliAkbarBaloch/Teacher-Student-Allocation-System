import { DataTable } from "@/components/common/DataTable";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { BulkImportDialog } from "@/features/teachers/components/BulkImportDialog";
import { TeacherDialogs } from "@/features/teachers/components/TeacherDialogs";
import { TeacherFilters } from "@/features/teachers/components/TeacherFilters";
import { TeachersPageHeader } from "@/features/teachers/components/TeachersPageHeader";
import { useTeachersPage } from "@/features/teachers/hooks/useTeachersPage";
import type {
  CreateTeacherRequest,
  Teacher,
  UpdateTeacherRequest,
} from "@/features/teachers/types/teacher.types";
import { useTeachersColumnConfig } from "@/features/teachers/utils/columnConfig";
import { useDialogState } from "@/hooks/useDialogState";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

export default function TeachersPage() {
  const navigate = useNavigate();
  const { t } = useTranslation("teachers");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const dialogs = useDialogState();
  const [isBulkImportDialogOpen, setIsBulkImportDialogOpen] = useState(false);

  const {
    teachers,
    selectedTeacher,
    //setSelectedTeacher,
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
    handleSearchChange,
    handleSchoolIdChange,
    handleEmploymentStatusChange,
    handleResetFilters,
    fetchTeacherDetails,
    handleCreateSubmit: handleCreateSubmitInternal,
    handleUpdateSubmit: handleUpdateSubmitInternal,
    handleDelete,
    handleOpenCreate,
    refreshList,
    isSubmitting,
    deleteTarget,
    setDeleteTarget,
  } = useTeachersPage();

  const handleOpenView = async (teacher: Teacher) => {
    await fetchTeacherDetails(teacher.id);
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
    dialogs.edit.setIsOpen(false);
  };

  const openDeleteDialog = (teacher: Teacher) => {
    setDeleteTarget(teacher);
    dialogs.delete.setIsOpen(true);
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    await handleDelete(deleteTarget);
    dialogs.delete.setIsOpen(false);
    setDeleteTarget(null);
  };

  const handleOpenCreateWithDialog = () => {
    handleOpenCreate();
    dialogs.create.setIsOpen(true);
  };

  const handleRowClick = (teacher: Teacher) => {
    navigate(`/base-data/teachers/${teacher.id}`);
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
          // Removed customActions for status change
          labels: {
            view: t("actions.view"),
            edit: t("actions.edit"),
            delete: t("actions.delete"),
          },
        }}
        enableRowClick
        onRowClick={handleRowClick}
      />

      <TeacherDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedTeacher={selectedTeacher}
        formLoading={formLoading}
        createFormKey={createFormKey}
        deleteTarget={deleteTarget}
        onCreateSubmit={handleCreateSubmit}
        onUpdateSubmit={handleUpdateSubmit}
        onDelete={confirmDelete}
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
