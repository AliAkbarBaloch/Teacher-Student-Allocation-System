import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { SchoolService } from "@/features/schools/services/schoolService";
import type {
  SchoolFilters as FiltersState,
  School,
  SchoolType,
} from "@/features/schools/types/school.types";
import { SchoolForm } from "@/features/schools/components/SchoolForm";
import { SchoolFilters } from "@/features/schools/components/SchoolFilters";
import { SchoolStatusBadge } from "@/features/schools/components/SchoolStatusBadge";
import { SchoolsPageHeader } from "@/features/schools/components/SchoolsPageHeader";
import { SchoolsTableSection } from "@/features/schools/components/SchoolsTableSection";
import { SchoolsPaginationControls } from "@/features/schools/components/SchoolsPaginationControls";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useDebounce } from "@/hooks/useDebounce";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { clampPage, getPaginationSummary, getVisiblePages } from "@/lib/utils/pagination";

type PaginationState = {
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
};

const DEFAULT_PAGINATION: PaginationState = {
  page: 1,
  pageSize: TABLE_PAGE_SIZE_OPTIONS[0],
  totalPages: 0,
  totalItems: 0,
};

export default function SchoolsPage() {
  const { t } = useTranslation("schools");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [schools, setSchools] = useState<School[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedSchool, setSelectedSchool] = useState<School | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [isCreateSubmitting, setIsCreateSubmitting] = useState(false);
  const [isUpdateSubmitting, setIsUpdateSubmitting] = useState(false);
  const [isStatusSubmitting, setIsStatusSubmitting] = useState(false);
  const [statusTarget, setStatusTarget] = useState<{ school: School | null; nextState: boolean }>({
    school: null,
    nextState: true,
  });

  const [searchInput, setSearchInput] = useState("");
  const [selectedType, setSelectedType] = useState<SchoolType | undefined>(undefined);
  const [zoneFilter, setZoneFilter] = useState<string | undefined>(undefined);
  const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all");
  const [pagination, setPagination] = useState<PaginationState>(DEFAULT_PAGINATION);
  const [isSearchInputLoading, setIsSearchInputLoading] = useState(false);

  const debouncedSearch = useDebounce(searchInput, 400);

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

      setSchools(response.items);
      setPagination((prev) => ({
        ...prev,
        page: response.page,
        pageSize: response.pageSize,
        totalItems: response.totalItems,
        totalPages: response.totalPages,
      }));
    } catch (err) {
      const message = err instanceof Error ? err.message : t("errors.load");
      setError(message);
    } finally {
      setLoading(false);
      setIsSearchInputLoading(false);
    }
  }, [filters, pagination.page, pagination.pageSize, t]);

  useEffect(() => {
    loadSchools();
  }, [loadSchools]);

  const refreshList = useCallback(async () => {
    await loadSchools();
  }, [loadSchools]);

  const handlePageChange = (newPage: number) => {
    setPagination((prev) => ({
      ...prev,
      page: clampPage(newPage, prev.totalPages),
    }));
  };

  const handlePageSizeChange = (size: number) => {
    setPagination((prev) => ({
      ...prev,
      page: 1,
      pageSize: size,
    }));
  };

  const handleOpenCreate = () => {
    setSelectedSchool(null);
    setIsCreateDialogOpen(true);
  };

  const handleOpenView = useCallback((school: School) => {
    setSelectedSchool(school);
    setIsViewDialogOpen(true);
  }, []);

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

  const handleOpenEdit = useCallback(async (school: School) => {
    try {
      await fetchSchoolDetails(school.id);
      setIsEditDialogOpen(true);
    } catch {
      // toast already handled
    }
  }, [fetchSchoolDetails]);

  const handleCreateSubmit = async (payload: Parameters<typeof SchoolService.create>[0]) => {
    setIsCreateSubmitting(true);
    try {
      await SchoolService.create(payload);
      toast.success(t("notifications.createSuccess"));
      setIsCreateDialogOpen(false);
      await refreshList();
    } finally {
      setIsCreateSubmitting(false);
    }
  };

  const handleUpdateSubmit = async (payload: Parameters<typeof SchoolService.update>[1]) => {
    if (!selectedSchool) return;
    setIsUpdateSubmitting(true);
    try {
      await SchoolService.update(selectedSchool.id, payload);
      toast.success(t("notifications.updateSuccess"));
      setIsEditDialogOpen(false);
      await refreshList();
    } finally {
      setIsUpdateSubmitting(false);
    }
  };

  const openStatusDialog = useCallback((school: School) => {
    setStatusTarget({ school, nextState: !school.isActive });
    setIsStatusDialogOpen(true);
  }, []);

  const confirmStatusChange = async () => {
    if (!statusTarget.school) return;
    setIsStatusSubmitting(true);
    try {
      await SchoolService.updateStatus(statusTarget.school.id, statusTarget.nextState);
      toast.success(
        statusTarget.nextState ? t("notifications.activateSuccess") : t("notifications.deactivateSuccess")
      );
      setIsStatusDialogOpen(false);
      await refreshList();
    } catch (error) {
      const message = error instanceof Error ? error.message : t("errors.submit");
      toast.error(message);
    } finally {
      setIsStatusSubmitting(false);
    }
  };

  const resetToFirstPage = useCallback(() => {
    setPagination((prev) => ({
      ...prev,
      page: 1,
    }));
  }, []);

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

  const paginationSummary = useMemo(
    () => getPaginationSummary(pagination.page, pagination.pageSize, pagination.totalItems),
    [pagination.page, pagination.pageSize, pagination.totalItems]
  );

  const visiblePages = useMemo(
    () => getVisiblePages(pagination.page, pagination.totalPages),
    [pagination.page, pagination.totalPages]
  );

  return (
    <div className="space-y-6">
      <SchoolsPageHeader
        isAdmin={isAdmin}
        onCreate={handleOpenCreate}
        title={t("title")}
        subtitle={t("subtitle")}
        createLabel={t("actions.create")}
        readOnlyTitle={t("permissions.readOnlyTitle")}
        readOnlyDescription={t("permissions.readOnlyDescription")}
      />

      <SchoolFilters
        searchValue={searchInput}
        onSearchChange={handleSearchChange}
        searchLoading={isSearchInputLoading}
        schoolType={selectedType}
        onSchoolTypeChange={handleSchoolTypeChange}
        zoneNumber={zoneFilter}
        onZoneNumberChange={handleZoneChange}
        status={statusFilter}
        onStatusChange={handleStatusFilterChange}
        onReset={handleResetFilters}
      />

      {error && (
        <div className="p-4 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          {error}
        </div>
      )}

      <SchoolsTableSection
        schools={schools}
        loading={loading}
        pageSize={pagination.pageSize}
        isAdmin={isAdmin}
        t={t}
        onViewSchool={handleOpenView}
        onEditSchool={handleOpenEdit}
        onToggleStatus={openStatusDialog}
      />

      {!loading && (
        <SchoolsPaginationControls
          paginationSummary={paginationSummary}
          pagination={pagination}
          pageSizeOptions={TABLE_PAGE_SIZE_OPTIONS}
          visiblePages={visiblePages}
          onPageChange={handlePageChange}
          onPageSizeChange={handlePageSizeChange}
          t={t}
        />
      )}

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <SchoolForm
            mode="create"
            onSubmit={handleCreateSubmit}
            onCancel={() => setIsCreateDialogOpen(false)}
            isSubmitting={isCreateSubmitting}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          {formLoading ? (
            <div className="flex min-h-[200px] items-center justify-center">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : (
            selectedSchool && (
              <SchoolForm
                mode="edit"
                school={selectedSchool}
                onSubmit={handleUpdateSubmit}
                onCancel={() => setIsEditDialogOpen(false)}
                isSubmitting={isUpdateSubmitting}
              />
            )
          )}
        </DialogContent>
      </Dialog>

      {/* View Dialog */}
      <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.view")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          {selectedSchool && (
            <div className="grid gap-4">
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.schoolName")}</p>
                <p className="text-base">{selectedSchool.schoolName}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.schoolType")}</p>
                <p>{t(`typeLabels.${selectedSchool.schoolType}`)}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.zoneNumber")}</p>
                <p>{selectedSchool.zoneNumber}</p>
              </div>
              {selectedSchool.address && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.address")}</p>
                  <p className="whitespace-pre-line">{selectedSchool.address}</p>
                </div>
              )}
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {selectedSchool.latitude && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.latitude")}</p>
                    <p>{selectedSchool.latitude}</p>
                  </div>
                )}
                {selectedSchool.longitude && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.longitude")}</p>
                    <p>{selectedSchool.longitude}</p>
                  </div>
                )}
                {selectedSchool.distanceFromCenter && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.distanceFromCenter")}
                    </p>
                    <p>{selectedSchool.distanceFromCenter}</p>
                  </div>
                )}
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.isActive")}</p>
                  <SchoolStatusBadge isActive={selectedSchool.isActive} />
                </div>
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {selectedSchool.contactEmail && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.contactEmail")}
                    </p>
                    <a
                      href={`mailto:${selectedSchool.contactEmail}`}
                      className="text-primary underline-offset-2 hover:underline"
                    >
                      {selectedSchool.contactEmail}
                    </a>
                  </div>
                )}
                {selectedSchool.contactPhone && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.contactPhone")}
                    </p>
                    <a
                      href={`tel:${selectedSchool.contactPhone}`}
                      className="text-primary underline-offset-2 hover:underline"
                    >
                      {selectedSchool.contactPhone}
                    </a>
                  </div>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Activate/Deactivate Confirmation */}
      <AlertDialog open={isStatusDialogOpen} onOpenChange={setIsStatusDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {statusTarget.nextState ? t("status.activateTitle") : t("status.deactivateTitle")}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {statusTarget.nextState ? t("status.activateDescription") : t("status.deactivateDescription")}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isStatusSubmitting}>{t("actions.cancel")}</AlertDialogCancel>
            <AlertDialogAction
              onClick={confirmStatusChange}
              disabled={isStatusSubmitting}
              className={statusTarget.nextState ? "" : "bg-destructive text-white hover:bg-destructive/90"}
            >
              {isStatusSubmitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : statusTarget.nextState ? (
                t("actions.activate")
              ) : (
                t("actions.deactivate")
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

