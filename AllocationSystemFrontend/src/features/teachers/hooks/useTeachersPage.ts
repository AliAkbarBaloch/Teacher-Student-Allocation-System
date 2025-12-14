import { useCallback, useEffect } from "react";
import { useTeachersData } from "./useTeachersData";
import { useTeachersFilters } from "./useTeachersFilters";
import { useTeachersDialogs } from "./useTeachersDialogs";
import type { Teacher, CreateTeacherRequest, UpdateTeacherRequest, EmploymentStatus } from "../types/teacher.types";

/**
 * Main hook that orchestrates teachers page functionality
 * Composes three focused hooks:
 * - useTeachersData: Data fetching and CRUD operations
 * - useTeachersFilters: Filter state management
 * - useTeachersDialogs: Dialog and submission state management
 */
export function useTeachersPage() {
  const filters = useTeachersFilters();
  const dialogs = useTeachersDialogs();
  const data = useTeachersData(filters.filters);

  const resetToFirstPage = useCallback(() => {
    data.handlePageChange(1);
  }, [data]);

  // Wrap filter handlers to reset page
  const handleSearchChange = useCallback(
    (value: string) => {
      filters.handleSearchChange(value, resetToFirstPage);
    },
    [filters, resetToFirstPage]
  );

  const handleSchoolIdChange = useCallback(
    (value?: number) => {
      filters.handleSchoolIdChange(value, resetToFirstPage);
    },
    [filters, resetToFirstPage]
  );

  const handleEmploymentStatusChange = useCallback(
    (value?: EmploymentStatus) => {
      filters.handleEmploymentStatusChange(value, resetToFirstPage);
    },
    [filters, resetToFirstPage]
  );

  const handleStatusFilterChange = useCallback(
    (value: "all" | "active" | "inactive") => {
      filters.handleStatusFilterChange(value, resetToFirstPage);
    },
    [filters, resetToFirstPage]
  );

  const handleResetFilters = useCallback(() => {
    filters.handleResetFilters(resetToFirstPage);
  }, [filters, resetToFirstPage]);

  // Wrap data handlers with dialog state management
  const handleCreateSubmit = useCallback(
    async (payload: CreateTeacherRequest) => {
      dialogs.setOperationInProgress("create");
      try {
        await data.createTeacher(payload);
      } finally {
        dialogs.setOperationInProgress(null);
      }
    },
    [data, dialogs]
  );

  const handleUpdateSubmit = useCallback(
    async (payload: UpdateTeacherRequest) => {
      if (!data.selectedTeacher) return;
      dialogs.setOperationInProgress("update");
      try {
        await data.updateTeacher(data.selectedTeacher.id, payload);
      } finally {
        dialogs.setOperationInProgress(null);
      }
    },
    [data, dialogs]
  );

  const handleStatusChange = useCallback(
    async (teacher: Teacher, nextState: EmploymentStatus) => {
      dialogs.setOperationInProgress("status");
      dialogs.setWarningMessage(null);
      try {
        await data.updateTeacherStatus(teacher.id, nextState);
        dialogs.closeStatusDialog();
      } catch (err: unknown) {
        const error = err as { message?: string; teacher?: Teacher };
        const message = error?.message || "An error occurred";
        // Check if error message indicates active allocation plan
        if (message.toLowerCase().includes("allocation") || message.toLowerCase().includes("active")) {
          dialogs.setWarningMessage(message);
        } else {
          dialogs.closeStatusDialog();
          throw err;
        }
      } finally {
        dialogs.setOperationInProgress(null);
      }
    },
    [data, dialogs]
  );

  const handleDelete = useCallback(
    async (teacher: Teacher) => {
      dialogs.setOperationInProgress("delete");
      try {
        await data.deleteTeacher(teacher.id);
        dialogs.closeDeleteDialog();
      } finally {
        dialogs.setOperationInProgress(null);
      }
    },
    [data, dialogs]
  );

  const handleOpenCreate = useCallback(() => {
    data.setSelectedTeacher(null);
    dialogs.incrementFormKey();
  }, [data, dialogs]);

  // Clear search loading when data finishes loading
  useEffect(() => {
    if (!data.loading) {
      filters.setIsSearchInputLoading(false);
    }
  }, [data.loading, filters]);

  return {
    // Data
    teachers: data.teachers,
    selectedTeacher: data.selectedTeacher,
    setSelectedTeacher: data.setSelectedTeacher,
    error: data.error,
    loading: data.loading,
    formLoading: data.formLoading,
    isSearchInputLoading: filters.isSearchInputLoading,
    createFormKey: dialogs.createFormKey,

    // Pagination
    pagination: data.pagination,
    handlePageChange: data.handlePageChange,
    handlePageSizeChange: data.handlePageSizeChange,

    // Filters
    searchInput: filters.searchInput,
    selectedSchoolId: filters.selectedSchoolId,
    selectedEmploymentStatus: filters.selectedEmploymentStatus,
    statusFilter: filters.statusFilter,
    handleSearchChange,
    handleSchoolIdChange,
    handleEmploymentStatusChange,
    handleStatusFilterChange,
    handleResetFilters,

    // Actions
    fetchTeacherDetails: data.fetchTeacherDetails,
    handleCreateSubmit,
    handleUpdateSubmit,
    handleStatusChange,
    handleDelete,
    handleOpenCreate,
    refreshList: data.refreshList,

    // Submitting state (consolidated)
    isSubmitting: dialogs.operationInProgress !== null,

    // Dialog targets
    statusTarget: dialogs.statusTarget,
    setStatusTarget: dialogs.setStatusTarget,
    deleteTarget: dialogs.deleteTarget,
    setDeleteTarget: dialogs.setDeleteTarget,
    warningMessage: dialogs.warningMessage,
    setWarningMessage: dialogs.setWarningMessage,
  };
}
