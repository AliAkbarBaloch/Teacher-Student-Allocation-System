import { useCallback, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Loader2 } from "lucide-react";

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
import type {
  School,
  CreateSchoolRequest,
  UpdateSchoolRequest,
} from "@/features/schools/types/school.types";
import { SchoolForm } from "@/features/schools/components/SchoolForm";
import { SchoolLocationMap } from "@/features/schools/components/SchoolLocationMap";
import { SchoolFilters } from "@/features/schools/components/SchoolFilters";
import { SchoolStatusBadge } from "@/features/schools/components/SchoolStatusBadge";
import { SchoolsPageHeader } from "@/features/schools/components/SchoolsPageHeader";
import { SchoolsTableSection } from "@/features/schools/components/SchoolsTableSection";
import { SchoolsPaginationControls } from "@/features/schools/components/SchoolsPaginationControls";
import { useSchoolsPage } from "@/features/schools/hooks/useSchoolsPage";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
import { getPaginationSummary, getVisiblePages } from "@/lib/utils/pagination";

export default function SchoolsPage() {
  const { t } = useTranslation("schools");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  const {
    schools,
    selectedSchool,
    setSelectedSchool,
    error,
    loading,
    formLoading,
    isSearchInputLoading,
    pagination,
    handlePageChange,
    handlePageSizeChange,
    searchInput,
    selectedType,
    zoneFilter,
    statusFilter,
    handleSearchChange,
    handleSchoolTypeChange,
    handleZoneChange,
    handleStatusFilterChange,
    handleResetFilters,
    fetchSchoolDetails,
    handleCreateSubmit: handleCreateSubmitInternal,
    handleUpdateSubmit: handleUpdateSubmitInternal,
    handleStatusChange,
    handleDelete,
    isCreateSubmitting,
    isUpdateSubmitting,
    isStatusSubmitting,
    isDeleteSubmitting,
    statusTarget,
    setStatusTarget,
    deleteTarget,
    setDeleteTarget,
  } = useSchoolsPage();

  const handleOpenCreate = () => {
    setSelectedSchool(null);
    setIsCreateDialogOpen(true);
  };

  const handleOpenView = useCallback((school: School) => {
    setSelectedSchool(school);
    setIsViewDialogOpen(true);
  }, [setSelectedSchool]);

  const handleOpenEdit = useCallback(async (school: School) => {
    try {
      await fetchSchoolDetails(school.id);
      setIsEditDialogOpen(true);
    } catch {
      // toast already handled
    }
  }, [fetchSchoolDetails]);

  const handleCreateSubmit = async (payload: CreateSchoolRequest) => {
    try {
      await handleCreateSubmitInternal(payload);
      setIsCreateDialogOpen(false);
    } catch {
      // Error already handled
    }
  };

  const handleUpdateSubmit = async (payload: UpdateSchoolRequest) => {
    try {
      await handleUpdateSubmitInternal(payload);
      setIsEditDialogOpen(false);
    } catch {
      // Error already handled
    }
  };

  const openStatusDialog = useCallback((school: School) => {
    setStatusTarget({ school, nextState: !school.isActive });
    setIsStatusDialogOpen(true);
  }, [setStatusTarget]);

  const openDeleteDialog = useCallback((school: School) => {
    setDeleteTarget(school);
    setIsDeleteDialogOpen(true);
  }, [setDeleteTarget]);

  const confirmStatusChange = async () => {
    if (!statusTarget.school) return;
    try {
      await handleStatusChange(statusTarget.school, statusTarget.nextState);
      setIsStatusDialogOpen(false);
      setStatusTarget({ school: null, nextState: false });
    } catch {
      // Error already handled
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await handleDelete(deleteTarget);
      setIsDeleteDialogOpen(false);
      setDeleteTarget(null);
    } catch {
      // Error already handled
    }
  };

  const paginationSummary = useMemo(
    () => getPaginationSummary(pagination.page, pagination.pageSize, pagination.totalItems),
    [pagination.page, pagination.pageSize, pagination.totalItems]
  );

  const visiblePages = useMemo(
    () => getVisiblePages(pagination.page, pagination.totalPages),
    [pagination.page, pagination.totalPages]
  );

  return (
    <div className="space-y-6 w-full min-w-0 max-w-full">
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
        onDeleteSchool={openDeleteDialog}
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
              {selectedSchool.transportAccessibility && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">
                    {t("form.fields.transportAccessibility")}
                  </p>
                  <p className="whitespace-pre-line">{selectedSchool.transportAccessibility}</p>
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
              {/* Location Map - at the bottom */}
              {(selectedSchool.latitude || selectedSchool.longitude) && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">Location Map</p>
                  <SchoolLocationMap
                    latitude={selectedSchool.latitude}
                    longitude={selectedSchool.longitude}
                    schoolName={selectedSchool.schoolName}
                    className="w-full"
                  />
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Activate/Deactivate Confirmation */}
      <AlertDialog
        open={isStatusDialogOpen}
        onOpenChange={(open) => {
          setIsStatusDialogOpen(open);
          if (!open) {
            setStatusTarget({ school: null, nextState: false });
          }
        }}
      >
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
              disabled={isStatusSubmitting || !statusTarget.school}
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

      {/* Delete Confirmation */}
      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t("delete.title")}</AlertDialogTitle>
            <AlertDialogDescription>{t("delete.description")}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleteSubmitting}>{t("delete.cancel")}</AlertDialogCancel>
            <AlertDialogAction
              onClick={confirmDelete}
              disabled={isDeleteSubmitting}
              className="bg-destructive text-white hover:bg-destructive/90"
            >
              {isDeleteSubmitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                t("delete.confirm")
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

