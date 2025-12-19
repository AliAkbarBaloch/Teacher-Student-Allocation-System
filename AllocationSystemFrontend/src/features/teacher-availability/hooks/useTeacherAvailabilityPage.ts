import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherAvailabilityService } from "../services/teacherSubjectService";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
} from "../types/teacherAvailability.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function useTeacherAvailabilityPage() {
  const { t } = useTranslation("teacherAvailability");

  const [teacherAvailabilities, setTeacherAvailabilities] = useState<TeacherAvailability[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedTeacherAvailability, setSelectedTeacherAvailability] = useState<TeacherAvailability | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadTeacherAvailabilities = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await TeacherAvailabilityService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "teacherFirstName",
        sortOrder: "asc",
      });

      setTeacherAvailabilities(response.items || []);
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
    loadTeacherAvailabilities();
  }, [loadTeacherAvailabilities]);

  const handleCreate = useCallback(async (data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest) => {
    setIsSubmitting(true);
    try {
      await TeacherAvailabilityService.create(data as CreateTeacherAvailabilityRequest);
      toast.success(t("create.success"));
      await loadTeacherAvailabilities();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherAvailabilities, t]);

  const handleUpdate = useCallback(async (data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await TeacherAvailabilityService.update(id, data as UpdateTeacherAvailabilityRequest);
      toast.success(t("update.success"));
      await loadTeacherAvailabilities();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadTeacherAvailabilities, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await TeacherAvailabilityService.delete(id);
      toast.success(t("delete.success"));
      await loadTeacherAvailabilities();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadTeacherAvailabilities, t]);

  return {
    teacherAvailabilities,
    loading,
    error,
    selectedTeacherAvailability,
    setSelectedTeacherAvailability,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadTeacherAvailabilities,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}