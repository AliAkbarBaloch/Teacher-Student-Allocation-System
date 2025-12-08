import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { ZoneConstraintService } from "../services/zoneConstraintService";
import type {
  ZoneConstraint,
  CreateZoneConstraintRequest,
  UpdateZoneConstraintRequest,
} from "../types/zoneConstraint.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function useZoneConstraintPage() {
  const { t } = useTranslation("zoneConstraints");

  const [zoneConstraints, setZoneConstraints] = useState<ZoneConstraint[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedZoneConstraint, setSelectedZoneConstraint] = useState<ZoneConstraint | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadZoneConstraints = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await ZoneConstraintService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "zoneNumber",
        sortOrder: "asc",
      });

      setZoneConstraints(response.items || []);
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
    loadZoneConstraints();
  }, [loadZoneConstraints]);

  const handleCreate = useCallback(async (data: CreateZoneConstraintRequest | UpdateZoneConstraintRequest) => {
    setIsSubmitting(true);
    try {
      await ZoneConstraintService.create(data as CreateZoneConstraintRequest);
      toast.success(t("create.success"));
      await loadZoneConstraints();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadZoneConstraints, t]);

  const handleUpdate = useCallback(async (data: UpdateZoneConstraintRequest | UpdateZoneConstraintRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await ZoneConstraintService.update(id, data as UpdateZoneConstraintRequest);
      toast.success(t("update.success"));
      await loadZoneConstraints();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadZoneConstraints, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await ZoneConstraintService.delete(id);
      toast.success(t("delete.success"));
      await loadZoneConstraints();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadZoneConstraints, t]);

  return {
    zoneConstraints,
    loading,
    error,
    selectedZoneConstraint,
    setSelectedZoneConstraint,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadZoneConstraints,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}