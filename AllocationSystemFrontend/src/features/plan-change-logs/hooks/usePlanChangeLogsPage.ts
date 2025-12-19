import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { PlanChangeLogService } from "../services/PlanChangeLogService";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function usePlanChangeLogsPage() {
  const { t } = useTranslation("planChangeLogs");

  const [planChangeLogs, setPlanChangeLogs] = useState<PlanChangeLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedPlanChangeLog, setSelectedPlanChangeLog] = useState<PlanChangeLog | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadPlanChangeLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await PlanChangeLogService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "changeType",
        sortOrder: "asc",
      });

      setPlanChangeLogs(response.items || []);
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
    loadPlanChangeLogs();
  }, [loadPlanChangeLogs]);

  const handleCreate = useCallback(async (data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest) => {
    setIsSubmitting(true);
    try {
      await PlanChangeLogService.create(data as CreatePlanChangeLogRequest);
      toast.success(t("create.success"));
      await loadPlanChangeLogs();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadPlanChangeLogs, t]);

  const handleUpdate = useCallback(async (data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await PlanChangeLogService.update(id, data);
      toast.success(t("update.success"));
      await loadPlanChangeLogs();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadPlanChangeLogs, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await PlanChangeLogService.delete(id);
      toast.success(t("delete.success"));
      await loadPlanChangeLogs();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadPlanChangeLogs, t]);

  return {
    planChangeLogs,
    loading,
    error,
    selectedPlanChangeLog,
    setSelectedPlanChangeLog,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadPlanChangeLogs,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}