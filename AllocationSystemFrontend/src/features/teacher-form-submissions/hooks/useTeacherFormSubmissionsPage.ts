import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherFormSubmissionService } from "../services/teacherFormSubmissionService";
import type {
  TeacherFormSubmission,
  TeacherFormSubmissionListParams,
  UpdateSubmissionStatusRequest,
} from "../types/teacherFormSubmission.types";

export function useTeacherFormSubmissionsPage() {
  const { t } = useTranslation("teacherFormSubmissions");

  const [submissions, setSubmissions] = useState<TeacherFormSubmission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedSubmission, setSelectedSubmission] = useState<TeacherFormSubmission | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0,
  });

  const loadSubmissions = useCallback(async (params: TeacherFormSubmissionListParams = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await TeacherFormSubmissionService.list({
        ...params,
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 10,
      });
      setSubmissions(data.items);
      setPagination({
        page: data.page,
        pageSize: data.pageSize,
        totalItems: data.totalItems,
        totalPages: data.totalPages,
      });
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("errors.loadFailed");
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  const fetchSubmissionDetails = useCallback(async (id: number) => {
    try {
      const submission = await TeacherFormSubmissionService.getById(id);
      setSelectedSubmission(submission);
      return submission;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("errors.loadFailed");
      toast.error(errorMessage);
      throw err;
    }
  }, [t]);

  const handleStatusUpdate = useCallback(
    async (id: number, isProcessed: boolean) => {
      setIsSubmitting(true);
      try {
        const updateRequest: UpdateSubmissionStatusRequest = { isProcessed };
        const updated = await TeacherFormSubmissionService.updateStatus(id, updateRequest);
        
        // Update in local state
        setSubmissions((prev) =>
          prev.map((s) => (s.id === id ? updated : s))
        );
        
        // Update selected submission if it's the one being updated
        if (selectedSubmission?.id === id) {
          setSelectedSubmission(updated);
        }
        
        toast.success(
          isProcessed ? t("notifications.markedProcessed") : t("notifications.markedUnprocessed")
        );
        return updated;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : t("errors.updateFailed");
        toast.error(errorMessage);
        throw err;
      } finally {
        setIsSubmitting(false);
      }
    },
    [t, selectedSubmission]
  );

  return {
    submissions,
    loading,
    error,
    selectedSubmission,
    setSelectedSubmission,
    isSubmitting,
    pagination,
    loadSubmissions,
    fetchSubmissionDetails,
    handleStatusUpdate,
  };
}

