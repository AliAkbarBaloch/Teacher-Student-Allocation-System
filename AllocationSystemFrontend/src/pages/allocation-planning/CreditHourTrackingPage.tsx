// react
import { useEffect, useCallback } from "react";
// translation
import { useTranslation } from "react-i18next";
// hooks
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useCreditHourTrackingPage } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingPage";
import { useCreditHourTrackingFiltersExtended } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingFiltersExtended";
import { useCreditHourTrackingDialogs } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingDialogs";
// components
import { DataTable } from "@/components/common/DataTable";
import { CreditHourTrackingFiltersContainer } from "@/features/credit-hour-tracking/components/CreditHourTrackingFiltersContainer";
import { CreditHourTrackingDialogsContainer } from "@/features/credit-hour-tracking/components/CreditHourTrackingDialogsContainer";
import { useCreditHourTrackingColumnConfig } from "@/features/credit-hour-tracking/utils/columnConfig";
// icons
import { Trash2 } from "lucide-react";
// constants
import { TABLE_PAGE_SIZE_OPTIONS } from "@/lib/constants/pagination";

export default function CreditHourTrackingPage() {
  const { t } = useTranslation("creditHourTracking");
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  // Core data management
  const {
    entries,
    loading,
    error,
    isSubmitting,
    pagination,
    loadEntries,
    handleUpdate,
    handleDelete,
  } = useCreditHourTrackingPage();

  // Extended filters with academic year loading, route handling, and client-side filtering
  const filters = useCreditHourTrackingFiltersExtended(entries);

  // Dialog management with action handlers
  const dialogs = useCreditHourTrackingDialogs(
    async (id, data) => {
      await handleUpdate(id, data);
    },
    async (id) => {
      await handleDelete(id);
    }
  );

  const columnConfig = useCreditHourTrackingColumnConfig();

  // Handle page change for server-side pagination
  const handlePageChange = useCallback((newPage: number) => {
    loadEntries({
      ...filters.filterParams,
      page: newPage,
      pageSize: pagination.pageSize,
      sortBy: "creditBalance",
      sortOrder: "desc",
    });
  }, [filters.filterParams, pagination.pageSize, loadEntries]);

  // Handle page size change for server-side pagination
  const handlePageSizeChange = useCallback((size: number) => {
    loadEntries({
      ...filters.filterParams,
      page: 1,
      pageSize: size,
      sortBy: "creditBalance",
      sortOrder: "desc",
    });
  }, [filters.filterParams, loadEntries]);

  // Load entries when server-side filters or pagination change
  useEffect(() => {
    loadEntries({
      ...filters.filterParams,
      page: pagination.page,
      pageSize: pagination.pageSize,
      sortBy: "creditBalance",
      sortOrder: "desc",
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters.filterParams, pagination.page, pagination.pageSize]);

  return (
    <div className="space-y-6 w-full min-w-0 max-w-full">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t("title")}</h1>
          <p className="text-sm text-muted-foreground mt-1">{t("subtitle")}</p>
        </div>
      </div>

      <CreditHourTrackingFiltersContainer filters={filters} disabled={loading} />

      <DataTable
        columnConfig={columnConfig}
        data={filters.filteredEntries}
        searchKey="teacherName"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={false}
        enableColumnVisibility={false}
        enablePagination={false}
        loading={loading}
        error={error}
        emptyMessage={
          entries.length === 0
            ? t("table.empty")
            : t("table.noFilteredResults")
        }
        disableInternalDialog={true}
        pageSizeOptions={[...TABLE_PAGE_SIZE_OPTIONS]}
        serverSidePagination={
          !filters.hasClientFilters
            ? {
                page: pagination.page,
                pageSize: pagination.pageSize,
                totalItems: pagination.totalItems,
                totalPages: pagination.totalPages,
                onPageChange: handlePageChange,
                onPageSizeChange: handlePageSizeChange,
              }
            : undefined
        }
        actions={{
          onView: dialogs.handleOpenView,
          customActions: isAdmin
            ? [
                {
                  label: () => t("actions.delete"),
                  icon: () => <Trash2 className="h-4 w-4" />,
                  onClick: dialogs.handleDeleteClick,
                  separator: true,
                },
              ]
            : undefined,
          labels: {
            view: t("actions.view"),
          },
        }}
      />

      <CreditHourTrackingDialogsContainer dialogs={dialogs} isSubmitting={isSubmitting} />
    </div>
  );
}
