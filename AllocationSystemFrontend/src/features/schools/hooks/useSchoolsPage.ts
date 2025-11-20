import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { SchoolService } from "../services/schoolService";
import type { School, SchoolFilters as FiltersState, SchoolType } from "../types/school.types";
import { usePagination } from "@/hooks/usePagination";
import { useDebounce } from "@/hooks/useDebounce";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";
import { SEARCH_DEBOUNCE_MS } from "@/lib/constants/app";

export function useSchoolsPage() {
  const { t } = useTranslation("schools");

  const [schools, setSchools] = useState<School[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedSchool, setSelectedSchool] = useState<School | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [isCreateSubmitting, setIsCreateSubmitting] = useState(false);
  const [isUpdateSubmitting, setIsUpdateSubmitting] = useState(false);
  const [isStatusSubmitting, setIsStatusSubmitting] = useState(false);
  const [isDeleteSubmitting, setIsDeleteSubmitting] = useState(false);
  const [statusTarget, setStatusTarget] = useState<{ school: School | null; nextState: boolean }>({
    school: null,
    nextState: true,
  });
  const [deleteTarget, setDeleteTarget] = useState<School | null>(null);

  const [searchInput, setSearchInput] = useState("");
  const [selectedType, setSelectedType] = useState<SchoolType | undefined>(undefined);
  const [zoneFilter, setZoneFilter] = useState<string | undefined>(undefined);
  const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all");
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);
  const [isSearchInputLoading, setIsSearchInputLoading] = useState(false);

  const debouncedSearch = useDebounce(searchInput, SEARCH_DEBOUNCE_MS);

  const filters = useMemo<FiltersState>(() => {
    const parsedZone =
      zoneFilter && !Number.isNaN(Number(zoneFilter)) ? Number(zoneFilter) : undefined;

    return {
      search: debouncedSearch || undefined,
      schoolType: selectedType,
      zoneNumber: parsedZone,
      isActive: statusFilter === "all" ? undefined : statusFilter === "active",
    };
  }, [debouncedSearch, selectedType, zoneFilter, statusFilter]);

  const loadSchools = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await SchoolService.list({
        ...filters,
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "schoolName",
        sortOrder: "asc",
      });

      setSchools(response.items || []);
      updatePagination({
        page: response.page || 1,
        pageSize: response.pageSize || pagination.pageSize,
        totalItems: response.totalItems || 0,
        totalPages: response.totalPages || 0,
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : t("errors.load");
      setError(message);
    } finally {
      setLoading(false);
      setIsSearchInputLoading(false);
    }
  }, [filters, pagination.page, pagination.pageSize, t, updatePagination]);

  useEffect(() => {
    loadSchools();
  }, [loadSchools]);

  const refreshList = useCallback(async () => {
    await loadSchools();
  }, [loadSchools]);

  const resetToFirstPage = useCallback(() => {
    handlePageChange(1);
  }, [handlePageChange]);

  const handleSearchChange = useCallback((value: string) => {
    setSearchInput(value);
    resetToFirstPage();
    setIsSearchInputLoading(true);
  }, [resetToFirstPage]);

  const handleSchoolTypeChange = useCallback((value?: SchoolType) => {
    setSelectedType(value);
    resetToFirstPage();
  }, [resetToFirstPage]);

  const handleZoneChange = useCallback((value?: string) => {
    setZoneFilter(value);
    resetToFirstPage();
  }, [resetToFirstPage]);

  const handleStatusFilterChange = useCallback((value: "all" | "active" | "inactive") => {
    setStatusFilter(value);
    resetToFirstPage();
  }, [resetToFirstPage]);

  const handleResetFilters = useCallback(() => {
    setSearchInput("");
    setSelectedType(undefined);
    setZoneFilter(undefined);
    setStatusFilter("all");
    resetToFirstPage();
    setIsSearchInputLoading(false);
  }, [resetToFirstPage]);

  const fetchSchoolDetails = useCallback(
    async (id: number) => {
      setFormLoading(true);
      try {
        const data = await SchoolService.getById(id);
        setSelectedSchool(data);
        return data;
      } catch (err) {
        const message = err instanceof Error ? err.message : t("errors.load");
        toast.error(message);
        throw err;
      } finally {
        setFormLoading(false);
      }
    },
    [t]
  );

  const handleCreateSubmit = useCallback(async (payload: Parameters<typeof SchoolService.create>[0]) => {
    setIsCreateSubmitting(true);
    let tempId: number | null = null;
    try {
      // Optimistic update
      tempId = -Date.now(); // Use negative ID to avoid conflicts
      const optimisticSchool: School = {
        id: tempId,
        schoolName: payload.schoolName,
        schoolType: payload.schoolType,
        zoneNumber: payload.zoneNumber,
        address: payload.address,
        latitude: payload.latitude,
        longitude: payload.longitude,
        distanceFromCenter: payload.distanceFromCenter,
        transportAccessibility: payload.transportAccessibility,
        contactEmail: payload.contactEmail,
        contactPhone: payload.contactPhone,
        isActive: payload.isActive ?? true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      setSchools((prev) => [optimisticSchool, ...prev]);

      const created = await SchoolService.create(payload);
      // Replace optimistic with real data
      setSchools((prev) => prev.map((s) => (s.id === tempId ? created : s)));
      toast.success(t("notifications.createSuccess"));
      await refreshList();
      return created;
    } catch (err) {
      // Rollback optimistic update
      if (tempId !== null) {
        setSchools((prev) => prev.filter((s) => s.id !== tempId));
      }
      const message = err instanceof Error ? err.message : t("errors.submit");
      toast.error(message);
      throw err;
    } finally {
      setIsCreateSubmitting(false);
    }
  }, [t, refreshList]);

  const handleUpdateSubmit = useCallback(async (payload: Parameters<typeof SchoolService.update>[1]) => {
    if (!selectedSchool) return;
    setIsUpdateSubmitting(true);
    try {
      // Optimistic update
      const optimisticUpdate: School = { ...selectedSchool, ...payload };
      setSchools((prev) => prev.map((s) => (s.id === selectedSchool.id ? optimisticUpdate : s)));

      const updated = await SchoolService.update(selectedSchool.id, payload);
      // Replace with real data
      setSchools((prev) => prev.map((s) => (s.id === selectedSchool.id ? updated : s)));
      setSelectedSchool(updated);
      toast.success(t("notifications.updateSuccess"));
      await refreshList();
      return updated;
    } catch (err) {
      // Rollback optimistic update
      setSchools((prev) => prev.map((s) => (s.id === selectedSchool.id ? selectedSchool : s)));
      const message = err instanceof Error ? err.message : t("errors.submit");
      toast.error(message);
      throw err;
    } finally {
      setIsUpdateSubmitting(false);
    }
  }, [selectedSchool, t, refreshList]);

  const handleStatusChange = useCallback(async (school: School, nextState: boolean) => {
    setStatusTarget({ school, nextState });
    setIsStatusSubmitting(true);
    try {
      // Optimistic update
      setSchools((prev) => prev.map((s) => (s.id === school.id ? { ...s, isActive: nextState } : s)));

      const updated = await SchoolService.updateStatus(school.id, nextState);
      // Replace with real data
      setSchools((prev) => prev.map((s) => (s.id === school.id ? updated : s)));
      toast.success(
        nextState ? t("notifications.activateSuccess") : t("notifications.deactivateSuccess")
      );
      setStatusTarget({ school: null, nextState: false });
      await refreshList();
      return updated;
    } catch (err) {
      // Rollback optimistic update
      setSchools((prev) => prev.map((s) => (s.id === school.id ? school : s)));
      const message = err instanceof Error ? err.message : t("errors.submit");
      toast.error(message);
      throw err;
    } finally {
      setIsStatusSubmitting(false);
    }
  }, [t, refreshList]);

  const handleDelete = useCallback(async (school: School) => {
    setDeleteTarget(school);
    setIsDeleteSubmitting(true);
    try {
      // Optimistic update
      const originalSchools = [...schools];
      setSchools((prev) => prev.filter((s) => s.id !== school.id));

      await SchoolService.delete(school.id);
      toast.success(t("notifications.deleteSuccess"));
      setDeleteTarget(null);
      await refreshList();
    } catch (err) {
      // Rollback optimistic update
      setSchools((prev) => {
        const index = prev.findIndex((s) => s.id === school.id);
        if (index === -1) {
          return [...prev, school].sort((a, b) => a.id - b.id);
        }
        return prev;
      });
      const message = err instanceof Error ? err.message : t("errors.submit");
      toast.error(message);
      throw err;
    } finally {
      setIsDeleteSubmitting(false);
    }
  }, [schools, t, refreshList]);

  return {
    // Data
    schools,
    selectedSchool,
    setSelectedSchool,
    error,
    loading,
    formLoading,
    isSearchInputLoading,
    
    // Pagination
    pagination,
    handlePageChange,
    handlePageSizeChange,
    
    // Filters
    searchInput,
    selectedType,
    zoneFilter,
    statusFilter,
    handleSearchChange,
    handleSchoolTypeChange,
    handleZoneChange,
    handleStatusFilterChange,
    handleResetFilters,
    
    // Actions
    fetchSchoolDetails,
    handleCreateSubmit,
    handleUpdateSubmit,
    handleStatusChange,
    handleDelete,
    refreshList,
    
    // Submitting states
    isCreateSubmitting,
    isUpdateSubmitting,
    isStatusSubmitting,
    isDeleteSubmitting,
    
    // Dialog targets
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
  };
}

