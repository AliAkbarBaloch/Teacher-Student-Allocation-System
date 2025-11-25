import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherSubjectService } from "../services/teacherSubjectService";
import type {
  TeacherSubject,
  CreateTeacherSubjectRequest,
  UpdateTeacherSubjectRequest,
} from "../types/teacherSubject.types";

export function useTeacherSubjectsPage() {
  const { t } = useTranslation("teacherSubjects");

  const [teacherSubjects, setTeacherSubjects] = useState<TeacherSubject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTeacherSubject, setSelectedTeacherSubject] = useState<TeacherSubject | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadTeacherSubjects = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await TeacherSubjectService.getAll();
      setTeacherSubjects(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadTeacherSubjects();
  }, [loadTeacherSubjects]);

  const handleCreate = useCallback(async (data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest) => {
    setIsSubmitting(true);
    try {
      await TeacherSubjectService.create(data as CreateTeacherSubjectRequest);
      toast.success(t("create.success"));
      await loadTeacherSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherSubjects, t]);

  const handleUpdate = useCallback(async (data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await TeacherSubjectService.update(id, data as UpdateTeacherSubjectRequest);
      toast.success(t("update.success"));
      await loadTeacherSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherSubjects, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await TeacherSubjectService.delete(id);
      toast.success(t("delete.success"));
      await loadTeacherSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadTeacherSubjects, t]);

  return {
    teacherSubjects,
    loading,
    error,
    selectedTeacherSubject,
    setSelectedTeacherSubject,
    isSubmitting,
    loadTeacherSubjects,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}