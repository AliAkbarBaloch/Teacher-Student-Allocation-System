import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherAvailabilityService } from "../services/teacherSubjectService";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
} from "../types/teacherAvailability.types";

export function useTeacherAvailabilityPage() {
  const { t } = useTranslation("teacherAvailability");

  const [teacherAvailabilities, setTeacherAvailabilities] = useState<TeacherAvailability[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTeacherAvailability, setSelectedTeacherAvailability] = useState<TeacherAvailability | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadTeacherAvailabilities = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await TeacherAvailabilityService.getAll();
      setTeacherAvailabilities(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

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
    loadTeacherAvailabilities,
    handleCreate,
    handleUpdate,
    handleDelete,
  };
}