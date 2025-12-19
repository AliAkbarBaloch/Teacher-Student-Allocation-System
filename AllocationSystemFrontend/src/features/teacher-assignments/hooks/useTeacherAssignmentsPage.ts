import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherAssignmentService } from "../services/teacherAssginmentService";
import type {
  TeacherAssignment,
  CreateTeacherAssignmentRequest,
  UpdateTeacherAssignmentRequest,
} from "../types/teacherAssignment.types";

export function useTeacherAssignmentsPage() {
  const { t } = useTranslation("teacherAssignments");

  const [teacherAssignments, setTeacherAssignments] = useState<TeacherAssignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAssignment, setSelectedAssignment] = useState<TeacherAssignment | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadTeacherAssignments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await TeacherAssignmentService.getAll();
      setTeacherAssignments(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadTeacherAssignments();
  }, [loadTeacherAssignments]);

  const handleCreate = useCallback(async (data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest) => {
    setIsSubmitting(true);
    try {
      await TeacherAssignmentService.create(data as CreateTeacherAssignmentRequest);
      toast.success(t("create.success"));
      await loadTeacherAssignments();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherAssignments, t]);

  const handleUpdate = useCallback(async (data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await TeacherAssignmentService.update(id, data as UpdateTeacherAssignmentRequest);
      toast.success(t("update.success"));
      await loadTeacherAssignments();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherAssignments, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await TeacherAssignmentService.delete(id);
      toast.success(t("delete.success"));
      await loadTeacherAssignments();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadTeacherAssignments, t]);

  return {
    teacherAssignments,
    loading,
    error,
    selectedAssignment,
    setSelectedAssignment,
    isSubmitting,
    loadTeacherAssignments,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}