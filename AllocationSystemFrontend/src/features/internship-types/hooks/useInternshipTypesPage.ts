import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { InternshipTypeService } from "../services/internshipTypeService";
import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
} from "../types/internshipType.types";

export function useInternshipTypesPage() {
  const { t } = useTranslation("internshipTypes");

  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedInternshipType, setSelectedInternshipType] = useState<InternshipType | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadInternshipTypes = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await InternshipTypeService.getAll();
      setInternshipTypes(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

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
    loadInternshipTypes,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}

