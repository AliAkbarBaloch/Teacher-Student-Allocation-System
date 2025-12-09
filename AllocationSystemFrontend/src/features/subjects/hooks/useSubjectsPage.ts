import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { SubjectService } from "../services/subjectService";
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
} from "../types/subject.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function useSubjectsPage() {
  const { t } = useTranslation("subjects");

  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadSubjects = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await SubjectService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "subjectTitle",
        sortOrder: "asc",
      });

      setSubjects(response.items || []);
      updatePagination({
        page: response.page || pagination.page,
        pageSize: response.pageSize || pagination.pageSize,
        totalItems: response.totalItems || 0,
        totalPages: response.totalPages || 0,
      });
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
    // updatePagination is stable (useCallback with no deps) - doesn't need to be in dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pagination.page, pagination.pageSize, t]);

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
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadSubjects,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}

