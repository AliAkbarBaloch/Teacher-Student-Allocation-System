import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { AllocationPlanService } from "../services/allocationPlanService";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

export function useAllocationPlansPage() {
  const { t } = useTranslation("allocationPlans");

  const [allocationPlans, setAllocationPlans] = useState<AllocationPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAllocationPlan, setSelectedAllocationPlan] = useState<AllocationPlan | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadAllocationPlans = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await AllocationPlanService.getAll();
      setAllocationPlans(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadAllocationPlans();
  }, [loadAllocationPlans]);

  const handleCreate = useCallback(async (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => {
    setIsSubmitting(true);
    try {
      await AllocationPlanService.create(data as CreateAllocationPlanRequest);
      toast.success(t("create.success"));
      await loadAllocationPlans();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadAllocationPlans, t]);

  const handleUpdate = useCallback(async (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await AllocationPlanService.update(id, data as UpdateAllocationPlanRequest);
      toast.success(t("update.success"));
      await loadAllocationPlans();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadAllocationPlans, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await AllocationPlanService.delete(id);
      toast.success(t("delete.success"));
      await loadAllocationPlans();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadAllocationPlans, t]);

  return {
    allocationPlans,
    loading,
    error,
    selectedAllocationPlan,
    setSelectedAllocationPlan,
    isSubmitting,
    loadAllocationPlans,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}