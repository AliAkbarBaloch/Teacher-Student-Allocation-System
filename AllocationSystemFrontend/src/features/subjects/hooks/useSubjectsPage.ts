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

  const loadSubjects = useCallback(async (page?: number, pageSize?: number) => {
    setLoading(true);
    setError(null);
    try {
      const targetPage = page ?? pagination.page;
      const targetPageSize = pageSize ?? pagination.pageSize;
      
      const response = await SubjectService.getPaginated({
        page: targetPage,
        pageSize: targetPageSize,
        sortBy: "subjectTitle",
        sortOrder: "asc",
      });

      setSubjects(response.items || []);
      updatePagination({
        page: response.page || targetPage,
        pageSize: response.pageSize || targetPageSize,
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
      // Reset to page 1 to show the new item (items are sorted by title)
      // Reload with page 1 explicitly to ensure data is fresh
      handlePageChange(1);
      await loadSubjects(1, pagination.pageSize);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjects, t, pagination.pageSize, handlePageChange]);

  const handleUpdate = useCallback(async (data: CreateSubjectRequest | UpdateSubjectRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await SubjectService.update(id, data as UpdateSubjectRequest);
      toast.success(t("update.success"));
      // Reload current page to reflect updates
      await loadSubjects(pagination.page, pagination.pageSize);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjects, t, pagination.page, pagination.pageSize]);

  const handleDelete = useCallback(async (id: number) => {
    setIsSubmitting(true);
    try {
      await SubjectService.delete(id);
      toast.success(t("delete.success"));
      // If current page becomes empty after deletion, go to previous page
      // Otherwise reload current page to reflect deletion
      const currentPageItemCount = subjects.length;
      const targetPage = currentPageItemCount === 1 && pagination.page > 1 
        ? pagination.page - 1 
        : pagination.page;
      
      if (targetPage !== pagination.page) {
        handlePageChange(targetPage);
      }
      // Always reload to ensure data is fresh
      await loadSubjects(targetPage, pagination.pageSize);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjects, t, subjects.length, pagination.page, pagination.pageSize, handlePageChange]);

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

