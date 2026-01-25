import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { AcademicYearService } from "../services/academicYearService";
import type { AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest } from "../types/academicYear.types";

type AcademicYearUpsert = CreateAcademicYearRequest | UpdateAcademicYearRequest;

function getErrorMessage(err: unknown, fallback: string): string {
  if (err instanceof Error) {
    return err.message;
  }
  return fallback;
}

function useAcademicYearsState() {
  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAcademicYear, setSelectedAcademicYear] = useState<AcademicYear | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  return {
    academicYears,
    setAcademicYears,
    loading,
    setLoading,
    error,
    setError,
    selectedAcademicYear,
    setSelectedAcademicYear,
    isSubmitting,
    setIsSubmitting,
  };
}

function useLoadAcademicYears(params: {
  t: (key: string) => string;
  setAcademicYears: (items: AcademicYear[]) => void;
  setLoading: (v: boolean) => void;
  setError: (v: string | null) => void;
}) {
  const { t, setAcademicYears, setLoading, setError } = params;

  return useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await AcademicYearService.getAll();
      setAcademicYears(data);
    } catch (err) {
      const message = getErrorMessage(err, t("table.emptyMessage"));
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }, [setAcademicYears, setError, setLoading, t]);
}

function useCreateAcademicYear(params: {
  t: (key: string) => string;
  loadAcademicYears: () => Promise<void>;
  setIsSubmitting: (v: boolean) => void;
}) {
  const { t, loadAcademicYears, setIsSubmitting } = params;

  return useCallback(
    async (data: AcademicYearUpsert) => {
      setIsSubmitting(true);
      try {
        await AcademicYearService.create(data as CreateAcademicYearRequest);
        toast.success(t("create.success"));
        await loadAcademicYears();
      } catch (err) {
        const message = getErrorMessage(err, t("create.error"));
        toast.error(message);
        throw new Error(message);
      } finally {
        setIsSubmitting(false);
      }
    },
    [loadAcademicYears, setIsSubmitting, t]
  );
}

function useUpdateAcademicYear(params: {
  t: (key: string) => string;
  loadAcademicYears: () => Promise<void>;
  setIsSubmitting: (v: boolean) => void;
}) {
  const { t, loadAcademicYears, setIsSubmitting } = params;

  return useCallback(
    async (data: AcademicYearUpsert, id: number) => {
      setIsSubmitting(true);
      try {
        await AcademicYearService.update(id, data as UpdateAcademicYearRequest);
        toast.success(t("update.success"));
        await loadAcademicYears();
      } catch (err) {
        const message = getErrorMessage(err, t("update.error"));
        toast.error(message);
        throw new Error(message);
      } finally {
        setIsSubmitting(false);
      }
    },
    [loadAcademicYears, setIsSubmitting, t]
  );
}

function useDeleteAcademicYear(params: {
  t: (key: string) => string;
  loadAcademicYears: () => Promise<void>;
  setError: (v: string | null) => void;
}) {
  const { t, loadAcademicYears, setError } = params;

  return useCallback(
    async (id: number) => {
      try {
        await AcademicYearService.delete(id);
        toast.success(t("delete.success"));
        await loadAcademicYears();
      } catch (err) {
        const message = getErrorMessage(err, t("delete.error"));
        setError(message);
        toast.error(message);
        throw new Error(message);
      }
    },
    [loadAcademicYears, setError, t]
  );
}
function buildAcademicYearsPageResult(state: ReturnType<typeof useAcademicYearsState>, params: {
  loadAcademicYears: () => Promise<void>;
  handleCreate: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  handleUpdate: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest, id: number) => Promise<void>;
  handleDelete: (id: number) => Promise<void>;
}) {
  return {
    academicYears: state.academicYears,
    loading: state.loading,
    error: state.error,
    selectedAcademicYear: state.selectedAcademicYear,
    setSelectedAcademicYear: state.setSelectedAcademicYear,
    isSubmitting: state.isSubmitting,
    loadAcademicYears: params.loadAcademicYears,
    handleCreate: params.handleCreate,
    handleUpdate: params.handleUpdate,
    handleDelete: params.handleDelete,
  };
}
function useLoadAcademicYearsForState(
  state: ReturnType<typeof useAcademicYearsState>,
  t: (key: string) => string
) {
  return useLoadAcademicYears({
    t,
    setAcademicYears: state.setAcademicYears,
    setLoading: state.setLoading,
    setError: state.setError,
  });
}


/**
 * Page hook for Academic Years CRUD + selection state.
 */
export function useAcademicYearsPage() {

  const { t } = useTranslation("academicYears");
  const state = useAcademicYearsState();
  const loadAcademicYears = useLoadAcademicYearsForState(state, t);

  useEffect(() => {
    loadAcademicYears();
  }, [loadAcademicYears]);

  const handleCreate = useCreateAcademicYear({
    t,
    loadAcademicYears,
    setIsSubmitting: state.setIsSubmitting,
  });

  const handleUpdate = useUpdateAcademicYear({
    t,
    loadAcademicYears,
    setIsSubmitting: state.setIsSubmitting,
  });

  const handleDelete = useDeleteAcademicYear({
    t,
    loadAcademicYears,
    setError: state.setError,
  });

  return buildAcademicYearsPageResult(state, {
    loadAcademicYears,
    handleCreate,
    handleUpdate,
    handleDelete,
  });

}
