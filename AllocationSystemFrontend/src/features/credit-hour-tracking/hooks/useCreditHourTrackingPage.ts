import { useCallback, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { CreditHourTrackingService } from "../services/creditHourTrackingService";
import type {
  CreditHourTracking,
  CreditHourTrackingListParams,
  UpdateCreditHourTrackingRequest,
} from "../types/creditHourTracking.types";

export function useCreditHourTrackingPage() {
  const { t } = useTranslation("creditHourTracking");

  const [entries, setEntries] = useState<CreditHourTracking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedEntry, setSelectedEntry] = useState<CreditHourTracking | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: 10,
    totalItems: 0,
    totalPages: 0,
  });

  const loadEntries = useCallback(async (params: CreditHourTrackingListParams = {}) => {
    setLoading(true);
    setError(null);
    try {
      const data = await CreditHourTrackingService.list({
        ...params,
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 10,
      });
      setEntries(data.items);
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


  const handleUpdate = useCallback(
    async (id: number, data: UpdateCreditHourTrackingRequest) => {
      setIsSubmitting(true);
      const currentEntry = entries.find((e) => e.id === id);
      const previousEntries = [...entries];
      const previousSelectedEntry = selectedEntry;
      
      try {
        // Optimistic update: update local state immediately for better UX
        if (currentEntry) {
          const optimisticEntry: CreditHourTracking = {
            ...currentEntry,
            ...data,
            notes: data.notes !== undefined ? data.notes : currentEntry.notes,
          };
          setEntries((prev) =>
            prev.map((e) => (e.id === id ? optimisticEntry : e))
          );
          if (selectedEntry?.id === id) {
            setSelectedEntry(optimisticEntry);
          }
        }

        // Perform actual update
        await CreditHourTrackingService.update(id, data);
        
        // Reload the entry to ensure all fields (including relationships) are properly loaded
        try {
          const updated = await CreditHourTrackingService.getById(id);
          
          // Validate the updated entry has all required fields
          if (updated?.teacherName && updated?.academicYearTitle) {
            // Update with server response
            setEntries((prev) =>
              prev.map((e) => (e.id === id ? updated : e)).filter((e): e is CreditHourTracking => 
                Boolean(e && e.teacherName && e.academicYearTitle)
              )
            );
            
            if (selectedEntry?.id === id) {
              setSelectedEntry(updated);
            }
            toast.success(t("notifications.updateSuccess"));
            return updated;
          }
        } catch (reloadError) {
          console.error("Failed to reload entry after update:", reloadError);
        }
        
        // Fallback: reload current page if individual reload fails
        await loadEntries({
          page: pagination.page,
          pageSize: pagination.pageSize,
          sortBy: "creditBalance",
          sortOrder: "desc",
        });
        
        toast.success(t("notifications.updateSuccess"));
        return currentEntry || null;
      } catch (err) {
        // Revert optimistic update on error
        setEntries(previousEntries);
        if (previousSelectedEntry) {
          setSelectedEntry(previousSelectedEntry);
        }
        
        const errorMessage = err instanceof Error ? err.message : t("errors.updateFailed");
        toast.error(errorMessage);
        throw err;
      } finally {
        setIsSubmitting(false);
      }
    },
    [t, selectedEntry, entries, pagination.page, pagination.pageSize, loadEntries]
  );

  const handleDelete = useCallback(
    async (id: number) => {
      setIsSubmitting(true);
      try {
        await CreditHourTrackingService.delete(id);
        
        // Remove from local state
        setEntries((prev) => prev.filter((e) => e.id !== id));
        
        // Clear selected entry if it's the one being deleted
        if (selectedEntry?.id === id) {
          setSelectedEntry(null);
        }
        
        toast.success(t("notifications.deleteSuccess"));
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : t("errors.deleteFailed");
        toast.error(errorMessage);
        throw err;
      } finally {
        setIsSubmitting(false);
      }
    },
    [t, selectedEntry]
  );

  return {
    entries,
    loading,
    error,
    selectedEntry,
    setSelectedEntry,
    isSubmitting,
    pagination,
    loadEntries,
    handleUpdate,
    handleDelete,
  };
}
