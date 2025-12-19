// react
import { useCallback, useEffect, useMemo, useState } from "react";
// translation
import { useTranslation } from "react-i18next";
// hooks
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useDialogState } from "@/hooks/useDialogState";
import { useTeacherFormSubmissionsPage } from "@/features/teacher-submissions/hooks/useTeacherFormSubmissionsPage";
import { useTeacherFormSubmissionFilters } from "@/features/teacher-submissions/hooks/useTeacherFormSubmissionFilters";
// components
import { DataTable } from "@/components/common/DataTable";
import { TeacherFormSubmissionFilters } from "@/features/teacher-submissions/components/TeacherFormSubmissionFilters";
import { TeacherFormSubmissionDialogs } from "@/features/teacher-submissions/components/TeacherFormSubmissionDialogs";
import { GenerateFormLinkDialog } from "@/features/teacher-submissions/components/GenerateFormLinkDialog";
import { useTeacherFormSubmissionColumnConfig } from "@/features/teacher-submissions/utils/columnConfig";
// types
import type { TeacherFormSubmission } from "@/features/teacher-submissions/types/teacherFormSubmission.types";
// icons
import { CheckCircle2, XCircle, Link2 } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
// constants
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";
// utils
import { getPaginationSummary } from "@/lib/utils/pagination";

export default function TeacherSubmissionsPage() {
  const { t } = useTranslation("teacherSubmissions");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const dialogs = useDialogState();
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [isGenerateLinkDialogOpen, setIsGenerateLinkDialogOpen] = useState(false);
  const [statusTarget, setStatusTarget] = useState<{
    submission: TeacherFormSubmission | null;
    nextStatus: boolean;
  }>({ submission: null, nextStatus: false });

  const filters = useTeacherFormSubmissionFilters();

  const {
    submissions,
    selectedSubmission,
    setSelectedSubmission,
    loading,
    error,
    isSubmitting,
    pagination,
    loadSubmissions,
    handleStatusUpdate,
  } = useTeacherFormSubmissionsPage();

  const columnConfig = useTeacherFormSubmissionColumnConfig();

  // Convert filter state to API params
  const filterParams = useMemo(() => {
    const params: {
      teacherId?: number;
      yearId?: number;
      isProcessed?: boolean;
    } = {};

    if (filters.filters.teacherId !== undefined) {
      params.teacherId = filters.filters.teacherId;
    }
    if (filters.filters.yearId !== undefined) {
      params.yearId = filters.filters.yearId;
    }
    if (filters.filters.isProcessed !== undefined) {
      params.isProcessed = filters.filters.isProcessed;
    }

    return params;
  }, [filters.filters]);

  // Load submissions when filters or pagination change
  useEffect(() => {
    loadSubmissions({
      ...filterParams,
      page: pagination.page,
      pageSize: pagination.pageSize,
      sortBy: "submittedAt",
      sortOrder: "desc",
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterParams, pagination.page, pagination.pageSize]);

  const handleOpenView = useCallback(
    (submission: TeacherFormSubmission) => {
      setSelectedSubmission(submission);
      dialogs.view.setIsOpen(true);
    },
    [setSelectedSubmission, dialogs.view]
  );

  const handleProcessedStatusChange = useCallback(
    (value: "all" | "processed" | "unprocessed") => {
      const isProcessed =
        value === "all" ? undefined : value === "processed" ? true : false;
      filters.handleFilterChange({ isProcessed });
    },
    [filters]
  );

  const handleYearChange = useCallback(
    (value?: number) => {
      filters.handleFilterChange({ yearId: value });
    },
    [filters]
  );

  const handleTeacherChange = useCallback(
    (value?: number) => {
      filters.handleFilterChange({ teacherId: value });
    },
    [filters]
  );

  const handleResetFilters = useCallback(() => {
    filters.handleResetFilters();
    // Reset will trigger reload with page 1
  }, [filters]);

  const handleStatusChange = useCallback(async () => {
    if (!statusTarget.submission) return;
    await handleStatusUpdate(statusTarget.submission.id, statusTarget.nextStatus);
  }, [statusTarget, handleStatusUpdate]);

  const openStatusDialog = useCallback(
    (submission: TeacherFormSubmission, nextStatus: boolean) => {
      setStatusTarget({ submission, nextStatus });
      setIsStatusDialogOpen(true);
    },
    []
  );

  const paginationSummary = useMemo(
    () =>
      getPaginationSummary(
        pagination.page,
        pagination.pageSize,
        pagination.totalItems
      ),
    [pagination.page, pagination.pageSize, pagination.totalItems]
  );
  const handlePageChange = useCallback((newPage: number) => {
    loadSubmissions({
      ...filterParams,
      page: newPage,
      pageSize: pagination.pageSize,
      sortBy: "submittedAt",
      sortOrder: "desc",
    });
  }, [filterParams, pagination.pageSize, loadSubmissions]);

  const handlePageSizeChange = useCallback((size: number) => {
    loadSubmissions({
      ...filterParams,
      page: 1,
      pageSize: size,
      sortBy: "submittedAt",
      sortOrder: "desc",
    });
  }, [filterParams, loadSubmissions]);

  return (
    <div className="space-y-6 w-full min-w-0 max-w-full">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t("title")}</h1>
          <p className="text-sm text-muted-foreground mt-1">{t("subtitle")}</p>
        </div>
        {isAdmin && (
          <Button onClick={() => setIsGenerateLinkDialogOpen(true)} className="gap-2">
            <Link2 className="h-4 w-4" />
            {t("actions.generateLink")}
          </Button>
        )}
      </div>

      <TeacherFormSubmissionFilters
        yearId={filters.filters.yearId}
        onYearIdChange={handleYearChange}
        teacherId={filters.filters.teacherId}
        onTeacherIdChange={handleTeacherChange}
        teacherSearch={filters.teacherSearch}
        onTeacherSearchChange={filters.setTeacherSearch}
        processedStatus={
          filters.filters.isProcessed === undefined
            ? "all"
            : filters.filters.isProcessed
            ? "processed"
            : "unprocessed"
        }
        onProcessedStatusChange={handleProcessedStatusChange}
        onReset={handleResetFilters}
        disabled={loading}
      />

      <DataTable
        columnConfig={columnConfig}
        data={submissions}
        searchKey="teacherName"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={false}
        enableColumnVisibility={false}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={t("table.empty")}
        disableInternalDialog={true}
        actions={{
          onView: handleOpenView,
          customActions: isAdmin
            ? [
                {
                  label: (submission: TeacherFormSubmission) =>
                    submission.isProcessed
                      ? t("actions.markUnprocessed")
                      : t("actions.markProcessed"),
                  icon: (submission: TeacherFormSubmission) =>
                    submission.isProcessed ? (
                      <XCircle className="h-4 w-4" />
                    ) : (
                      <CheckCircle2 className="h-4 w-4" />
                    ),
                  onClick: (submission: TeacherFormSubmission) =>
                    openStatusDialog(submission, !submission.isProcessed),
                  separator: false,
                },
              ]
            : undefined,
          labels: {
            view: t("actions.view"),
          },
        }}
      />

      {/* Pagination Controls */}
      {!loading && pagination.totalItems > 0 && (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="text-sm text-muted-foreground">
            {t("table.pagination.display", {
              from: paginationSummary.from,
              to: paginationSummary.to,
              total: pagination.totalItems,
            })}
          </div>
          <div className="flex items-center gap-2">
            <select
              value={pagination.pageSize}
              onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              className="h-9 rounded-md border border-input bg-background px-3 py-1 text-sm"
            >
              {TABLE_PAGE_SIZE_OPTIONS.map((size) => (
                <option key={size} value={size}>
                  {size} {t("table.perPage")}
                </option>
              ))}
            </select>
            <div className="flex gap-1">
              <button
                onClick={() => handlePageChange(pagination.page - 1)}
                disabled={pagination.page <= 1}
                className="h-9 px-3 rounded-md border border-input bg-background text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {t("table.previous")}
              </button>
              <button
                onClick={() => handlePageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages}
                className="h-9 px-3 rounded-md border border-input bg-background text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {t("table.next")}
              </button>
            </div>
          </div>
        </div>
      )}

      <TeacherFormSubmissionDialogs
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isStatusDialogOpen={isStatusDialogOpen}
        setIsStatusDialogOpen={setIsStatusDialogOpen}
        selectedSubmission={selectedSubmission}
        statusTarget={statusTarget}
        setStatusTarget={setStatusTarget}
        onStatusChange={handleStatusChange}
        isSubmitting={isSubmitting}
      />

      <GenerateFormLinkDialog
        open={isGenerateLinkDialogOpen}
        onOpenChange={setIsGenerateLinkDialogOpen}
        onLinkGenerated={() => {
          // Refresh submissions after generating a link
          // Reset to page 1 and sort by id desc to show newly generated links first (newest records have highest IDs)
          loadSubmissions({
            ...filterParams,
            page: 1,
            pageSize: pagination.pageSize,
            sortBy: "id",
            sortOrder: "desc",
          });
        }}
      />
    </div>
  );
}

