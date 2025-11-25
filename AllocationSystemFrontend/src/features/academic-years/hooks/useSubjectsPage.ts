import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { AcademicYearService } from "../services/academicYearService";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";

export function useAcademicYearsPage() {
  const { t } = useTranslation("academicYears");

  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAcademicYear, setSelectedAcademicYear] = useState<AcademicYear | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadAcademicYears = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await AcademicYearService.getAll();
      setAcademicYears(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadAcademicYears();
  }, [loadAcademicYears]);

  const handleCreate = useCallback(async (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => {
    setIsSubmitting(true);
    try {
      await AcademicYearService.create(data as CreateAcademicYearRequest);
      toast.success(t("create.success"));
      await loadAcademicYears();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadAcademicYears, t]);

  const handleUpdate = useCallback(async (data: CreateAcademicYearRequest | UpdateAcademicYearRequest, id: number) => {
    setIsSubmitting(true);
    try {
      await AcademicYearService.update(id, data as UpdateAcademicYearRequest);
      toast.success(t("update.success"));
      await loadAcademicYears();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  }, [loadAcademicYears, t]);

  const handleDelete = useCallback(async (id: number) => {
    try {
      await AcademicYearService.delete(id);
      toast.success(t("delete.success"));
      await loadAcademicYears();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
      toast.error(errorMessage);
      throw new Error(errorMessage);
    }
  }, [loadAcademicYears, t]);

  return {
    academicYears,
    loading,
    error,
    selectedAcademicYear,
    setSelectedAcademicYear,
    isSubmitting,
    loadAcademicYears,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}