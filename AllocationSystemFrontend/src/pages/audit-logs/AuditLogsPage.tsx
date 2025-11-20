// react
import React, { useState, useEffect, useCallback, useMemo } from "react";
// translations
import { useTranslation } from "react-i18next";
// router
import { useSearchParams } from "react-router-dom";
import { Download, ChevronDown, ChevronRight, ChevronLeft, ChevronsLeft, ChevronsRight } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";

import { Skeleton } from "@/components/ui/skeleton";
import {
  Collapsible,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";

// features
import { AuditLogService } from "@/features/audit-logs/services/auditLogService";
import { AuditLogFilters } from "@/features/audit-logs/components/AuditLogFilters";
import { JsonDiffViewer } from "@/features/audit-logs/components/JsonDiffViewer";

// types
import type {
  AuditLog,
  AuditLogFilters as AuditLogFiltersType,
} from "@/features/audit-logs/types/auditLog.types";
import { AuditAction } from "@/features/audit-logs/types/auditLog.types";

// constants
import { DEFAULT_AUDIT_LOG_FILTERS, PAGE_SIZE_OPTIONS } from "@/features/audit-logs/constants/filters";
import { AUDIT_LOG_TABLE_COLUMN_COUNT } from "@/features/audit-logs/constants/table";
import { getActionBadgeVariant } from "@/features/audit-logs/utils/badgeVariants";
import { formatDate } from "@/lib/utils/date";
import { downloadBlob, generateFilename } from "@/lib/utils/fileDownload";

// utils
import { parseFiltersFromUrl, filtersToUrlParams } from "@/features/audit-logs/utils/urlParams";

export default function AuditLogsPage() {
  const { t } = useTranslation("auditLogs");
  const [searchParams, setSearchParams] = useSearchParams();
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());
  const [filters, setFilters] = useState<AuditLogFiltersType>(() => 
    parseFiltersFromUrl(searchParams)
  );
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [exporting, setExporting] = useState(false);
  // Maintain a set of all entities encountered across all filter states
  const [allAvailableEntities, setAllAvailableEntities] = useState<Set<string>>(new Set());

  // Extract unique entities from current audit logs and merge with all encountered entities
  useEffect(() => {
    setAllAvailableEntities((prevEntities) => {
      const entities = new Set<string>(prevEntities);
      auditLogs.forEach((log) => {
        if (log.targetEntity) {
          entities.add(log.targetEntity);
        }
      });
      return entities;
    });
  }, [auditLogs]);

  // Convert to sorted array for use in filters
  const availableEntities = useMemo(() => {
    return Array.from(allAvailableEntities).sort();
  }, [allAvailableEntities]);

  // Available actions are static, no need for useMemo
  const availableActions = Object.values(AuditAction);

  // Sync URL when filters change (but not on initial mount to avoid double render)
  const isInitialMount = React.useRef(true);
  useEffect(() => {
    if (isInitialMount.current) {
      isInitialMount.current = false;
      return;
    }
    
    const urlParams = filtersToUrlParams(filters);
    const newSearch = urlParams.toString();
    const currentSearch = searchParams.toString();
    
    // Only update URL if it's different to avoid unnecessary navigation
    if (newSearch !== currentSearch) {
      setSearchParams(urlParams, { replace: true });
    }
  }, [filters, searchParams, setSearchParams]);

  const loadAuditLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await AuditLogService.getAuditLogs(filters);
      setAuditLogs(response.content);
      setTotalElements(response.totalElements);
      setTotalPages(response.totalPages);
      setCurrentPage(response.number);
      setPageSize(response.size);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : t("errors.loadFailed");
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [filters, t]);

  useEffect(() => {
    loadAuditLogs();
  }, [loadAuditLogs]);

  const handleFiltersChange = (newFilters: AuditLogFiltersType) => {
    setFilters({
      ...newFilters,
      page: 0, // Reset to first page when filters change
      size: newFilters.size || filters.size || 20,
      sortBy: newFilters.sortBy || filters.sortBy || "eventTimestamp",
      sortDirection: newFilters.sortDirection || filters.sortDirection || "DESC",
    });
  };

  const handleResetFilters = () => {
    setFilters(DEFAULT_AUDIT_LOG_FILTERS);
  };

  const toggleRow = (id: number) => {
    const newExpanded = new Set(expandedRows);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedRows(newExpanded);
  };

  const handlePageChange = (newPage: number) => {
    setFilters({
      ...filters,
      page: newPage,
    });
  };

  const handlePageSizeChange = (newSize: number) => {
    setFilters({
      ...filters,
      size: newSize,
      page: 0,
    });
  };

  const handleSortChange = (sortBy: string, sortDirection: "ASC" | "DESC") => {
    setFilters({
      ...filters,
      sortBy,
      sortDirection,
      page: 0,
    });
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await AuditLogService.exportAuditLogs(filters);
      downloadBlob(blob, generateFilename("audit-logs"));
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : t("errors.exportFailed");
      setError(errorMessage);
    } finally {
      setExporting(false);
    }
  };

  return (
    <ErrorBoundary>
      <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">
            {t("title")}
          </h2>
          <p className="text-muted-foreground text-sm mt-1">{t("subtitle")}</p>
        </div>
        <Button
          onClick={handleExport}
          disabled={exporting || loading}
          variant="outline"
        >
          <Download className="mr-2 h-4 w-4" />
          {exporting ? t("actions.exporting") : t("actions.export")}
        </Button>
      </div>

      <AuditLogFilters
        filters={filters}
        onFiltersChange={handleFiltersChange}
        onReset={handleResetFilters}
        availableEntities={availableEntities}
        availableActions={availableActions}
      />

      {error && (
        <div className="p-4 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          {error}
        </div>
      )}

      <div className="border rounded-lg overflow-hidden">
        <div className="overflow-x-auto relative">
          {loading && auditLogs.length > 0 && (
            <div className="absolute inset-0 bg-background/50 backdrop-blur-sm z-10 flex items-center justify-center">
              <div className="flex flex-col items-center gap-2">
                <div className="h-8 w-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
                <p className="text-sm text-muted-foreground">{t("table.loading")}</p>
              </div>
            </div>
          )}
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12"></TableHead>
                <TableHead className="min-w-[120px]">
                  <button
                    onClick={() =>
                      handleSortChange(
                        "eventTimestamp",
                        filters.sortDirection === "DESC" ? "ASC" : "DESC"
                      )
                    }
                    className="flex items-center gap-1 hover:text-foreground"
                    aria-label={`Sort by ${t("table.timestamp")} ${filters.sortBy === "eventTimestamp" && filters.sortDirection === "DESC" ? "ascending" : "descending"}`}
                    aria-sort={filters.sortBy === "eventTimestamp" ? (filters.sortDirection === "DESC" ? "descending" : "ascending") : "none"}
                  >
                    {t("table.timestamp")}
                    {filters.sortBy === "eventTimestamp" &&
                      (filters.sortDirection === "DESC" ? " ↓" : " ↑")}
                  </button>
                </TableHead>
                <TableHead className="min-w-[150px]">
                  {t("table.user")}
                </TableHead>
                <TableHead className="min-w-[100px]">
                  <button
                    onClick={() =>
                      handleSortChange(
                        "action",
                        filters.sortDirection === "DESC" ? "ASC" : "DESC"
                      )
                    }
                    className="flex items-center gap-1 hover:text-foreground"
                    aria-label={`Sort by ${t("table.action")} ${filters.sortBy === "action" && filters.sortDirection === "DESC" ? "ascending" : "descending"}`}
                    aria-sort={filters.sortBy === "action" ? (filters.sortDirection === "DESC" ? "descending" : "ascending") : "none"}
                  >
                    {t("table.action")}
                    {filters.sortBy === "action" &&
                      (filters.sortDirection === "DESC" ? " ↓" : " ↑")}
                  </button>
                </TableHead>
                <TableHead className="min-w-[120px]">
                  {t("table.entity")}
                </TableHead>
                <TableHead className="min-w-[100px]">
                  {t("table.recordId")}
                </TableHead>
                <TableHead className="min-w-[200px]">
                  {t("table.description")}
                </TableHead>
                <TableHead className="min-w-[120px]">
                  {t("table.ipAddress")}
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                Array.from({ length: pageSize }).map((_, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <Skeleton className="h-4 w-4" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-32" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-24" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-20" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-24" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-16" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-48" />
                    </TableCell>
                    <TableCell>
                      <Skeleton className="h-4 w-24" />
                    </TableCell>
                  </TableRow>
                ))
              ) : auditLogs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={AUDIT_LOG_TABLE_COLUMN_COUNT} className="h-24 text-center">
                    <div className="text-muted-foreground">
                      {t("table.emptyMessage")}
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                auditLogs.map((log) => {
                  const isExpanded = expandedRows.has(log.id);
                  const hasValues = log.previousValue || log.newValue;

                  return (
                    <React.Fragment key={log.id}>
                      <TableRow
                        className={hasValues ? "cursor-pointer" : ""}
                        onClick={() => hasValues && toggleRow(log.id)}
                        onKeyDown={(e) => {
                          if (hasValues && (e.key === "Enter" || e.key === " ")) {
                            e.preventDefault();
                            toggleRow(log.id);
                          }
                        }}
                        role={hasValues ? "button" : "row"}
                        tabIndex={hasValues ? 0 : undefined}
                        aria-expanded={hasValues ? isExpanded : undefined}
                        aria-label={hasValues ? `Toggle details for audit log ${log.id}` : undefined}
                      >
                        <TableCell>
                          {hasValues && (
                            <Collapsible
                              open={isExpanded}
                              onOpenChange={() => toggleRow(log.id)}
                            >
                              <CollapsibleTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-6 w-6 p-0"
                                >
                                  {isExpanded ? (
                                    <ChevronDown className="h-4 w-4" />
                                  ) : (
                                    <ChevronRight className="h-4 w-4" />
                                  )}
                                </Button>
                              </CollapsibleTrigger>
                            </Collapsible>
                          )}
                        </TableCell>
                        <TableCell className="text-sm">
                          {formatDate(log.eventTimestamp)}
                        </TableCell>
                        <TableCell className="text-sm">
                          {log.userIdentifier}
                        </TableCell>
                        <TableCell>
                          <Badge 
                            variant={getActionBadgeVariant(log.action)}
                            className="text-xs font-normal"
                          >
                            {log.action}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-sm">
                          {log.targetEntity}
                        </TableCell>
                        <TableCell className="text-sm font-mono">
                          {log.targetRecordId || "-"}
                        </TableCell>
                        <TableCell className="text-sm text-muted-foreground">
                          {log.description || "-"}
                        </TableCell>
                        <TableCell className="text-sm font-mono">
                          {log.ipAddress || "-"}
                        </TableCell>
                      </TableRow>
                      {isExpanded && hasValues && (
                        <TableRow>
                          <TableCell colSpan={AUDIT_LOG_TABLE_COLUMN_COUNT} className="p-4 bg-muted/30">
                            <div className="space-y-4">
                              <h4 className="text-sm font-semibold">
                                {t("table.valueChanges")}
                              </h4>
                              <JsonDiffViewer
                                previousValue={log.previousValue}
                                newValue={log.newValue}
                              />
                            </div>
                          </TableCell>
                        </TableRow>
                      )}
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* Pagination */}
      {!loading && auditLogs.length > 0 && (
        <div className="flex flex-col gap-4 w-full">
          {/* Top row: Info and rows per page */}
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 w-full">
            <p className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
              {t("pagination.showing", {
                from: currentPage * pageSize + 1,
                to: Math.min((currentPage + 1) * pageSize, totalElements),
                total: totalElements,
              })}
            </p>
            <div className="flex items-center gap-2">
              <p className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap hidden sm:inline">
                {t("pagination.rowsPerPage")}
              </p>
              <p className="text-xs text-muted-foreground whitespace-nowrap sm:hidden">
                Rows:
              </p>
              <Select
                value={String(pageSize)}
                onValueChange={(value) => handlePageSizeChange(Number(value))}
              >
                <SelectTrigger className="h-8 w-[80px] text-xs sm:text-sm min-w-[80px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {PAGE_SIZE_OPTIONS.map((size) => (
                    <SelectItem key={size} value={String(size)}>
                      {size}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          
          {/* Bottom row: Navigation buttons */}
          <div className="flex items-center justify-center gap-1 sm:gap-2 w-full">
            {/* First button - hidden on mobile */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(0)}
              disabled={currentPage === 0}
              className="hidden md:flex items-center gap-1 h-8 px-2 sm:px-3"
            >
              <ChevronsLeft className="h-4 w-4" />
              <span className="hidden lg:inline">{t("pagination.first")}</span>
            </Button>
            
            {/* Previous button */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="flex items-center gap-1 h-8 px-2 sm:px-3"
              aria-label={t("pagination.previous")}
            >
              <ChevronLeft className="h-4 w-4" />
              <span className="hidden sm:inline">{t("pagination.previous")}</span>
            </Button>
            
            {/* Page info */}
            <div className="flex items-center gap-2 px-2 sm:px-4">
              <p className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
                <span className="hidden sm:inline">
                  {t("pagination.page", {
                    current: currentPage + 1,
                    total: totalPages,
                  })}
                </span>
                <span className="sm:hidden">
                  {currentPage + 1} / {totalPages}
                </span>
              </p>
            </div>
            
            {/* Next button */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
              className="flex items-center gap-1 h-8 px-2 sm:px-3"
              aria-label={t("pagination.next")}
            >
              <span className="hidden sm:inline">{t("pagination.next")}</span>
              <ChevronRight className="h-4 w-4" />
            </Button>
            
            {/* Last button - hidden on mobile */}
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageChange(totalPages - 1)}
              disabled={currentPage >= totalPages - 1}
              className="hidden md:flex items-center gap-1 h-8 px-2 sm:px-3"
            >
              <span className="hidden lg:inline">{t("pagination.last")}</span>
              <ChevronsRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}
      </div>
    </ErrorBoundary>
  );
}

