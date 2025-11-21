import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { SubjectCategoryService } from "../services/subjectCategoryService";
import type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
} from "../types/subjectCategory.types";

export function useSubjectCategoriesPage() {
  const { t } = useTranslation("subjectCategories");

  const [subjectCategories, setSubjectCategories] = useState<SubjectCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedSubjectCategory, setSelectedSubjectCategory] = useState<SubjectCategory | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const loadSubjectCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await SubjectCategoryService.getAll();
      setSubjectCategories(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadSubjectCategories();
  }, [loadSubjectCategories]);

  const handleCreate = useCallback(async (data: CreateSubjectCategoryRequest | UpdateSubjectCategoryRequest) => {
    setIsSubmitting(true);
    setFormError(null);
    try {
      await SubjectCategoryService.create(data as CreateSubjectCategoryRequest);
      toast.success(t("create.success"));
      setFormError(null);
      await loadSubjectCategories();
    } catch (err) {
      let errorMessage = t("create.error");
      if (err instanceof Error) {
        errorMessage = err.message;
        // Check if it's a 409 conflict error
        if ((err as Error & { status?: number }).status === 409) {
          errorMessage = err.message || t("create.errorDuplicate");
        }
      }
      setFormError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjectCategories, t]);

  const handleUpdate = useCallback(async (data: CreateSubjectCategoryRequest | UpdateSubjectCategoryRequest, id: number) => {
    setIsSubmitting(true);
    setFormError(null);
    try {
      await SubjectCategoryService.update(id, data as UpdateSubjectCategoryRequest);
      toast.success(t("update.success"));
      setFormError(null);
      await loadSubjectCategories();
    } catch (err) {
      let errorMessage = t("update.error");
      if (err instanceof Error) {
        errorMessage = err.message;
        // Check if it's a 409 conflict error
        if ((err as Error & { status?: number }).status === 409) {
          errorMessage = err.message || t("update.errorDuplicate");
        }
      }
      setFormError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadSubjectCategories, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await SubjectCategoryService.delete(id);
      toast.success(t("delete.success"));
      await loadSubjectCategories();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadSubjectCategories, t]);

  return {
    subjectCategories,
    loading,
    error,
    selectedSubjectCategory,
    setSelectedSubjectCategory,
    isSubmitting,
    formError,
    setFormError,
    loadSubjectCategories,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}

