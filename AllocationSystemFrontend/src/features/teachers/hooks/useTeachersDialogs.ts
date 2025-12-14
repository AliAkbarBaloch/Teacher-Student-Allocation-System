import { useCallback, useState } from "react";
import type { Teacher, EmploymentStatus } from "../types/teacher.types";

export type OperationType = "create" | "update" | "status" | "delete" | null;

export function useTeachersDialogs() {
  // Consolidated loading state - only one operation can be in progress at a time
  const [operationInProgress, setOperationInProgress] = useState<OperationType>(null);
  const [statusTarget, setStatusTarget] = useState<{ teacher: Teacher | null; nextStatus: EmploymentStatus | null }>({
    teacher: null,
    nextStatus: null,
  });
  const [deleteTarget, setDeleteTarget] = useState<Teacher | null>(null);
  const [warningMessage, setWarningMessage] = useState<string | null>(null);
  const [createFormKey, setCreateFormKey] = useState(0);

  const openStatusDialog = useCallback((teacher: Teacher, nextStatus: EmploymentStatus) => {
    setStatusTarget({ teacher, nextStatus });
    setWarningMessage(null);
  }, []);

  const openDeleteDialog = useCallback((teacher: Teacher) => {
    setDeleteTarget(teacher);
  }, []);

  const closeStatusDialog = useCallback(() => {
    setStatusTarget({ teacher: null, nextStatus: null });
    setWarningMessage(null);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setDeleteTarget(null);
  }, []);

  const incrementFormKey = useCallback(() => {
    setCreateFormKey((prev) => prev + 1);
  }, []);

  return {
    // Consolidated loading state
    operationInProgress,
    setOperationInProgress,
    // Convenience getters for backward compatibility (can be removed later)
    isCreateSubmitting: operationInProgress === "create",
    isUpdateSubmitting: operationInProgress === "update",
    isStatusSubmitting: operationInProgress === "status",
    isDeleteSubmitting: operationInProgress === "delete",
    // Dialog targets
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
    warningMessage,
    setWarningMessage,
    createFormKey,
    // Actions
    openStatusDialog,
    openDeleteDialog,
    closeStatusDialog,
    closeDeleteDialog,
    incrementFormKey,
  };
}