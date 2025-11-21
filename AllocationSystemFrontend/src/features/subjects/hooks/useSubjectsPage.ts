import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { SubjectService } from "../services/subjectService";
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
} from "../types/subject.types";

export function useSubjectsPage() {
  const { t } = useTranslation("subjects");

  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadSubjects = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await SubjectService.getAll();
      setSubjects(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadSubjects();
  }, [loadSubjects]);

  const handleCreate = useCallback(async (data: CreateSubjectRequest | UpdateSubjectRequest) => {
    setIsSubmitting(true);
    try {
      await SubjectService.create(data as CreateSubjectRequest);
      toast.success(t("create.success"));
      await loadSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjects, t]);

  const handleUpdate = useCallback(async (data: CreateSubjectRequest | UpdateSubjectRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await SubjectService.update(id, data as UpdateSubjectRequest);
      toast.success(t("update.success"));
      await loadSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjects, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await SubjectService.delete(id);
      toast.success(t("delete.success"));
      await loadSubjects();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadSubjects, t]);

  return {
    subjects,
    loading,
    error,
    selectedSubject,
    setSelectedSubject,
    isSubmitting,
    loadSubjects,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}

