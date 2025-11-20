import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { TeacherService } from "../services/teacherService";
import type { Teacher, EmploymentStatus } from "../types/teacher.types";
import { usePagination } from "@/hooks/usePagination";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";

type TeachersDataFilters = {
  search?: string;
  schoolId?: number;
  employmentStatus?: EmploymentStatus;
  isActive?: boolean;
};

export function useTeachersData(filters: TeachersDataFilters) {
  const { t } = useTranslation("teachers");

  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedTeacher, setSelectedTeacher] = useState<Teacher | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const { pagination, handlePageChange, handlePageSizeChange, updatePagination } = usePagination(DEFAULT_TABLE_PAGE_SIZE);

  const loadTeachers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await TeacherService.list({
        ...filters,
        page: pagination.page,
        pageSize: pagination.pageSize,
        sortBy: "lastName",
        sortOrder: "asc",
      });

      setTeachers(response.items || []);
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
    }
  }, [filters, pagination.page, pagination.pageSize, t, updatePagination]);

  useEffect(() => {
    loadTeachers();
  }, [loadTeachers]);

  const refreshList = useCallback(async () => {
    await loadTeachers();
  }, [loadTeachers]);

  const fetchTeacherDetails = useCallback(
    async (id: number) => {
      setFormLoading(true);
      try {
        const data = await TeacherService.getById(id);
        setSelectedTeacher(data);
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

  const createTeacher = useCallback(
    async (payload: Parameters<typeof TeacherService.create>[0]) => {
      // Optimistic update
      const tempId = -Date.now();
      const optimisticTeacher: Teacher = {
        id: tempId,
        schoolId: payload.schoolId,
        schoolName: "", // Will be filled from response
        firstName: payload.firstName,
        lastName: payload.lastName,
        email: payload.email,
        phone: payload.phone || null,
        isPartTime: payload.isPartTime,
        employmentStatus: payload.employmentStatus,
        usageCycle: payload.usageCycle || null,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      setTeachers((prev) => [optimisticTeacher, ...prev]);

      try {
        const created = await TeacherService.create(payload);
        // Replace optimistic with real data
        setTeachers((prev) => prev.map((t) => (t.id === tempId ? created : t)));
        toast.success(t("notifications.createSuccess"));
        await refreshList();
        return created;
      } catch (err) {
        // Rollback optimistic update
        setTeachers((prev) => prev.filter((t) => t.id !== tempId));
        const message = err instanceof Error ? err.message : t("errors.submit");
        toast.error(message);
        throw err;
      }
    },
    [t, refreshList]
  );

  const updateTeacher = useCallback(
    async (id: number, payload: Parameters<typeof TeacherService.update>[1]) => {
      const teacher = teachers.find((t) => t.id === id);
      if (!teacher) return;

      // Optimistic update
      const optimisticUpdate: Teacher = { ...teacher, ...payload };
      setTeachers((prev) => prev.map((t) => (t.id === id ? optimisticUpdate : t)));

      try {
        const updated = await TeacherService.update(id, payload);
        // Replace with real data
        setTeachers((prev) => prev.map((t) => (t.id === id ? updated : t)));
        if (selectedTeacher?.id === id) {
          setSelectedTeacher(updated);
        }
        toast.success(t("notifications.updateSuccess"));
        await refreshList();
        return updated;
      } catch (err) {
        // Rollback optimistic update
        setTeachers((prev) => prev.map((t) => (t.id === id ? teacher : t)));
        const message = err instanceof Error ? err.message : t("errors.submit");
        toast.error(message);
        throw err;
      }
    },
    [teachers, selectedTeacher, t, refreshList]
  );

  const updateTeacherStatus = useCallback(
    async (id: number, isActive: boolean) => {
      const teacher = teachers.find((t) => t.id === id);
      if (!teacher) return;

      // Optimistic update
      setTeachers((prev) => prev.map((t) => (t.id === id ? { ...t, isActive } : t)));

      try {
        const updated = await TeacherService.updateStatus(id, isActive);
        // Replace with real data
        setTeachers((prev) => prev.map((t) => (t.id === id ? updated : t)));
        toast.success(
          isActive ? t("notifications.activateSuccess") : t("notifications.deactivateSuccess")
        );
        await refreshList();
        return updated;
      } catch (err) {
        // Rollback optimistic update
        setTeachers((prev) => prev.map((t) => (t.id === id ? teacher : t)));
        const message = err instanceof Error ? err.message : t("errors.submit");
        throw { message, teacher }; // Return error with teacher for warning handling
      }
    },
    [teachers, t, refreshList]
  );

  const deleteTeacher = useCallback(
    async (id: number) => {
      const teacher = teachers.find((t) => t.id === id);
      if (!teacher) return;

      // Save original state for rollback
      const originalTeachers = [...teachers];
      // Optimistic update
      setTeachers((prev) => prev.filter((t) => t.id !== id));

      try {
        await TeacherService.delete(id);
        toast.success(t("notifications.deleteSuccess"));
        await refreshList();
      } catch (err) {
        // Rollback optimistic update
        setTeachers(originalTeachers);
        const message = err instanceof Error ? err.message : t("errors.submit");
        toast.error(message);
        throw err;
      }
    },
    [teachers, t, refreshList]
  );

  return {
    // Data
    teachers,
    selectedTeacher,
    setSelectedTeacher,
    loading,
    error,
    formLoading,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    // Actions
    fetchTeacherDetails,
    createTeacher,
    updateTeacher,
    updateTeacherStatus,
    deleteTeacher,
    refreshList,
  };
}

