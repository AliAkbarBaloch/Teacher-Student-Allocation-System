import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { PlanChangeLogService } from "../services/PlanChangeLogService";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";

export function usePlanChangeLogsPage() {
  const { t } = useTranslation("planChangeLogs");

  const [planChangeLogs, setPlanChangeLogs] = useState<PlanChangeLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedPlanChangeLog, setSelectedPlanChangeLog] = useState<PlanChangeLog | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadPlanChangeLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await PlanChangeLogService.getAll();
      setPlanChangeLogs(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

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
    loadPlanChangeLogs,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}