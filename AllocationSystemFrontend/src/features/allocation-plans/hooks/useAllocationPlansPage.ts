import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { AllocationPlanService } from "../services/allocationPlanService";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function useAllocationPlansPage() {
  const { t } = useTranslation("allocationPlans");

  const [allocationPlans, setAllocationPlans] = useState<AllocationPlan[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedAllocationPlan, setSelectedAllocationPlan] =
    useState<AllocationPlan | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const {
    pagination,
    handlePageChange,
    handlePageSizeChange,
    updatePagination,
  } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadAllocationPlans = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await AllocationPlanService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "planName",
        sortOrder: "asc",
      });

      setAllocationPlans(response.items || []);
      updatePagination({
        page: response.page || pagination.page,
        pageSize: response.pageSize || pagination.pageSize,
        totalItems: response.totalItems || 0,
        totalPages: response.totalPages || 0,
      });
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
    // updatePagination is stable (useCallback with no deps) - doesn't need to be in dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pagination.page, pagination.pageSize, t]);

  useEffect(() => {
    loadAllocationPlans();
  }, [loadAllocationPlans]);

  const handleCreate = useCallback(
    async (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => {
      setIsSubmitting(true);
      try {
        // Import AllocationService dynamically
        const { AllocationService } = await import(
          "@/services/allocationService"
        );

        // Trigger allocation algorithm first
        const createData = data as CreateAllocationPlanRequest;
        const allocationResult = await AllocationService.runAllocation(
          createData.yearId
        );

        toast.success(
          t("create.success") + ` - Plan ID: ${allocationResult.planId}`
        );
        await loadAllocationPlans();
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : t("create.error");
        toast.error(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setIsSubmitting(false);
      }
    },
    [loadAllocationPlans, t]
  );

  const handleUpdate = useCallback(
    async (
      data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest,
      id: number
    ) => {
      setIsSubmitting(true);
      try {
        await AllocationPlanService.update(
          id,
          data as UpdateAllocationPlanRequest
        );
        toast.success(t("update.success"));
        await loadAllocationPlans();
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : t("update.error");
        toast.error(errorMessage);
        throw new Error(errorMessage);
      } finally {
        setIsSubmitting(false);
      }
    },
    [loadAllocationPlans, t]
  );

  const handleDelete = useCallback(
    async (id: number) => {
      try {
        await AllocationPlanService.delete(id);
        toast.success(t("delete.success"));
        await loadAllocationPlans();
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : t("delete.error");
        setError(errorMessage);
        toast.error(errorMessage);
        throw new Error(errorMessage);
      }
    },
    [loadAllocationPlans, t]
  );

  return {
    allocationPlans,
    loading,
    error,
    selectedAllocationPlan,
    setSelectedAllocationPlan,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadAllocationPlans,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}
