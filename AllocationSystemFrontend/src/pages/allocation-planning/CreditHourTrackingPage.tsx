// react
import { useEffect } from "react";
// translation
import { useTranslation } from "react-i18next";
// hooks
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useCreditHourTrackingPage } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingPage";
import { useCreditHourTrackingFiltersExtended } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingFiltersExtended";
import { useCreditHourTrackingDialogs } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingDialogs";
import { useCreditHourTrackingPagination } from "@/features/credit-hour-tracking/hooks/useCreditHourTrackingPagination";
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

  // Pagination logic
  const { paginationSummary, handlePageChange, handlePageSizeChange } = useCreditHourTrackingPagination({
    pagination,
    filteredEntries: filters.filteredEntries,
    hasClientFilters: filters.hasClientFilters,
    filterParams: filters.filterParams,
    loadEntries,
  });

  const columnConfig = useCreditHourTrackingColumnConfig();

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

      {/* Pagination Controls */}
      {!loading && filters.filteredEntries.length > 0 && (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="text-sm text-muted-foreground">
            {filters.hasClientFilters ? (
              // Show filtered count when client-side filtering is active
              t("table.pagination.display", {
                from: 1,
                to: filters.filteredEntries.length,
                total: filters.filteredEntries.length,
              })
            ) : (
              // Show server-side pagination info
              t("table.pagination.display", {
                from: paginationSummary.from,
                to: paginationSummary.to,
                total: pagination.totalItems,
              })
            )}
            {filters.hasClientFilters && (
              <span className="ml-2 text-xs text-muted-foreground">
                ({t("table.filteredResults")})
              </span>
            )}
          </div>
          {!filters.hasClientFilters && (
            <div className="flex items-center gap-2">
              <select
                value={pagination.pageSize}
                onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                className="h-9 rounded-md border border-input bg-background px-3 py-1 text-sm"
                aria-label="Items per page"
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
                  className="h-9 px-3 rounded-md border border-input bg-background text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-accent transition-colors"
                  aria-label="Previous page"
                >
                  {t("table.previous")}
                </button>
                <button
                  onClick={() => handlePageChange(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages}
                  className="h-9 px-3 rounded-md border border-input bg-background text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-accent transition-colors"
                  aria-label="Next page"
                >
                  {t("table.next")}
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Empty State with Filters */}
      {!loading && filters.filteredEntries.length === 0 && entries.length === 0 && !error && (
        <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
          <div className="text-muted-foreground mb-2">
            <p className="text-lg font-medium">{t("table.empty")}</p>
            <p className="text-sm mt-1">{t("table.emptyDescription")}</p>
          </div>
        </div>
      )}

      <CreditHourTrackingDialogsContainer dialogs={dialogs} isSubmitting={isSubmitting} />
    </div>
  );
}
