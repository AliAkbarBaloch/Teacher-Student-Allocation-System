import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useTeachersPage } from "@/features/teachers/hooks/useTeachersPage";
import { TeacherDialogs } from "@/features/teachers/components/TeacherDialogs";
import { TeacherFilters } from "@/features/teachers/components/TeacherFilters";
import { TeachersPageHeader } from "@/features/teachers/components/TeachersPageHeader";
import { TeachersTableSection } from "@/features/teachers/components/TeachersTableSection";
import { TeachersPaginationControls } from "@/features/teachers/components/TeachersPaginationControls";
import { BulkImportDialog } from "@/features/teachers/components/BulkImportDialog";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { getPaginationSummary, getVisiblePages } from "@/lib/utils/pagination";
import type { Teacher, CreateTeacherRequest, UpdateTeacherRequest } from "@/features/teachers/types/teacher.types";

export default function TeachersPage() {
  const { t } = useTranslation("teachers");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
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
    isCreateSubmitting,
    isUpdateSubmitting,
    isStatusSubmitting,
    isDeleteSubmitting,
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
    warningMessage,
    setWarningMessage,
  } = useTeachersPage();

  const handleOpenView = (teacher: Teacher) => {
    setSelectedTeacher(teacher);
    setIsViewDialogOpen(true);
  };

  const handleOpenEdit = async (teacher: Teacher) => {
    try {
      await fetchTeacherDetails(teacher.id);
      setIsEditDialogOpen(true);
    } catch {
      // Error already handled
    }
  };

  const handleCreateSubmit = async (payload: CreateTeacherRequest) => {
    try {
      await handleCreateSubmitInternal(payload);
      setIsCreateDialogOpen(false);
    } catch {
      // Error already handled
    }
  };

  const handleUpdateSubmit = async (payload: UpdateTeacherRequest) => {
    try {
      await handleUpdateSubmitInternal(payload);
      setIsEditDialogOpen(false);
    } catch {
      // Error already handled
    }
  };

  const openStatusDialog = (teacher: Teacher) => {
    setStatusTarget({ teacher, nextState: !teacher.isActive });
    setWarningMessage(null);
    setIsStatusDialogOpen(true);
  };

  const openDeleteDialog = (teacher: Teacher) => {
    setDeleteTarget(teacher);
    setIsDeleteDialogOpen(true);
  };

  const confirmStatusChange = async () => {
    if (!statusTarget.teacher) return;
    try {
      await handleStatusChange(statusTarget.teacher, statusTarget.nextState);
      setIsStatusDialogOpen(false);
      setStatusTarget({ teacher: null, nextState: false });
      setWarningMessage(null);
    } catch {
      // Error already handled (warning message set if needed)
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await handleDelete(deleteTarget);
      setIsDeleteDialogOpen(false);
      setDeleteTarget(null);
    } catch {
      // Error already handled
    }
  };

  const handleOpenCreateWithDialog = () => {
    handleOpenCreate();
    setIsCreateDialogOpen(true);
  };

  const handleCloseStatus = () => {
    setStatusTarget({ teacher: null, nextState: false });
    setWarningMessage(null);
  };

  const paginationSummary = useMemo(
    () => getPaginationSummary(pagination.page, pagination.pageSize, pagination.totalItems),
    [pagination.page, pagination.pageSize, pagination.totalItems]
  );

  const visiblePages = useMemo(
    () => getVisiblePages(pagination.page, pagination.totalPages),
    [pagination.page, pagination.totalPages]
  );

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

      {error && (
        <div className="p-4 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          {error}
        </div>
      )}

      <TeachersTableSection
        teachers={teachers}
        loading={loading}
        pageSize={pagination.pageSize}
        isAdmin={isAdmin}
        t={t}
        onViewTeacher={handleOpenView}
        onEditTeacher={handleOpenEdit}
        onToggleStatus={openStatusDialog}
        onDeleteTeacher={openDeleteDialog}
      />

      {!loading && (
        <TeachersPaginationControls
          paginationSummary={paginationSummary}
          pagination={pagination}
          pageSizeOptions={TABLE_PAGE_SIZE_OPTIONS}
          visiblePages={visiblePages}
          onPageChange={handlePageChange}
          onPageSizeChange={handlePageSizeChange}
          t={t}
        />
      )}

      <TeacherDialogs
        isCreateDialogOpen={isCreateDialogOpen}
        setIsCreateDialogOpen={setIsCreateDialogOpen}
        isEditDialogOpen={isEditDialogOpen}
        setIsEditDialogOpen={setIsEditDialogOpen}
        isViewDialogOpen={isViewDialogOpen}
        setIsViewDialogOpen={setIsViewDialogOpen}
        isStatusDialogOpen={isStatusDialogOpen}
        setIsStatusDialogOpen={setIsStatusDialogOpen}
        isDeleteDialogOpen={isDeleteDialogOpen}
        setIsDeleteDialogOpen={setIsDeleteDialogOpen}
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
        isCreateSubmitting={isCreateSubmitting}
        isUpdateSubmitting={isUpdateSubmitting}
        isStatusSubmitting={isStatusSubmitting}
        isDeleteSubmitting={isDeleteSubmitting}
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

