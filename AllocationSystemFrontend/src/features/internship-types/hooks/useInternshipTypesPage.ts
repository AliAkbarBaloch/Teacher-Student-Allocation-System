import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { InternshipTypeService } from "../services/internshipTypeService";
import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
} from "../types/internshipType.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

export function useInternshipTypesPage() {
  const { t } = useTranslation("internshipTypes");

  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedInternshipType, setSelectedInternshipType] = useState<InternshipType | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadInternshipTypes = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await InternshipTypeService.getPaginated({
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "fullName",
        sortOrder: "asc",
      });

      setInternshipTypes(response.items || []);
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
    loadInternshipTypes();
  }, [loadInternshipTypes]);

  const handleCreate = useCallback(async (data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest) => {
    setIsSubmitting(true);
    try {
      await InternshipTypeService.create(data as CreateInternshipTypeRequest);
      toast.success(t("create.success"));
      await loadInternshipTypes();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadInternshipTypes, t]);

  const handleUpdate = useCallback(async (data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await InternshipTypeService.update(id, data as UpdateInternshipTypeRequest);
      toast.success(t("update.success"));
      await loadInternshipTypes();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadInternshipTypes, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await InternshipTypeService.delete(id);
      toast.success(t("delete.success"));
      await loadInternshipTypes();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadInternshipTypes, t]);

  return {
    internshipTypes,
    loading,
    error,
    selectedInternshipType,
    setSelectedInternshipType,
    isSubmitting,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    loadInternshipTypes,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}

