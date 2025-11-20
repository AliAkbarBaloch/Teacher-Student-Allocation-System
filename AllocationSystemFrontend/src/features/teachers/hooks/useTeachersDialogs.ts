import { useCallback, useState } from "react";
import type { Teacher } from "../types/teacher.types";

export function useTeachersDialogs() {
  const [isCreateSubmitting, setIsCreateSubmitting] = useState(false);
  const [isUpdateSubmitting, setIsUpdateSubmitting] = useState(false);
  const [isStatusSubmitting, setIsStatusSubmitting] = useState(false);
  const [isDeleteSubmitting, setIsDeleteSubmitting] = useState(false);
  const [statusTarget, setStatusTarget] = useState<{ teacher: Teacher | null; nextState: boolean }>({
    teacher: null,
    nextState: true,
  });
  const [deleteTarget, setDeleteTarget] = useState<Teacher | null>(null);
  const [warningMessage, setWarningMessage] = useState<string | null>(null);
  const [createFormKey, setCreateFormKey] = useState(0);

  const openStatusDialog = useCallback((teacher: Teacher) => {
    setStatusTarget({ teacher, nextState: !teacher.isActive });
    setWarningMessage(null);
  }, []);

  const openDeleteDialog = useCallback((teacher: Teacher) => {
    setDeleteTarget(teacher);
  }, []);

  const closeStatusDialog = useCallback(() => {
    setStatusTarget({ teacher: null, nextState: false });
    setWarningMessage(null);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setDeleteTarget(null);
  }, []);

  const incrementFormKey = useCallback(() => {
    setCreateFormKey((prev) => prev + 1);
  }, []);

  return {
    // Submitting states
    isCreateSubmitting,
    setIsCreateSubmitting,
    isUpdateSubmitting,
    setIsUpdateSubmitting,
    isStatusSubmitting,
    setIsStatusSubmitting,
    isDeleteSubmitting,
    setIsDeleteSubmitting,
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

