// react
import { useState, useEffect } from "react";
// translations
import { useTranslation } from "react-i18next";
// components
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
// icons
import { Filter, X, Loader2 } from "lucide-react";
// types
import type { AuditLogFilters as AuditLogFiltersType, AuditAction } from "../types/auditLog.types";
// utils
import { formatDateForInput, parseDateInput } from "@/lib/utils/date";
// hooks
import { useDebounce } from "@/hooks/useDebounce";

/**
 * Formats a string to title case (first letter capitalized, rest lowercase)
 * Handles underscores by converting them to spaces
 * Examples: "CREATE" -> "Create", "LOGIN_FAILED" -> "Login Failed"
 */
function formatToTitleCase(value: string): string {
  return value
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(" ");
}

interface AuditLogFiltersProps {
  filters: AuditLogFiltersType;
  onFiltersChange: (filters: AuditLogFiltersType) => void;
  onReset: () => void;
  availableEntities: string[];
  availableActions: AuditAction[];
}

export function AuditLogFilters({
  filters,
  onFiltersChange,
  onReset,
  availableEntities,
  availableActions,
}: AuditLogFiltersProps) {
  const { t } = useTranslation("auditLogs");
  const [userSearchInput, setUserSearchInput] = useState(filters.userSearch || "");
  const [startDateInput, setStartDateInput] = useState(formatDateForInput(filters.startDate));
  const [endDateInput, setEndDateInput] = useState(formatDateForInput(filters.endDate));
  const [isUserSearchLoading, setIsUserSearchLoading] = useState(false);

  // Debounce user search input (500ms delay for API calls)
  const debouncedUserSearch = useDebounce(userSearchInput, 500);
  
  // Debounce date inputs (300ms delay)
  const debouncedStartDate = useDebounce(startDateInput, 300);
  const debouncedEndDate = useDebounce(endDateInput, 300);

  // Update filters when debounced values change
  useEffect(() => {
    if (debouncedUserSearch !== filters.userSearch) {
      setIsUserSearchLoading(true);
      onFiltersChange({
        ...filters,
        userSearch: debouncedUserSearch.trim() || undefined,
      });
      // Reset loading state after a short delay
      setTimeout(() => setIsUserSearchLoading(false), 100);
    }
  }, [debouncedUserSearch]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (debouncedStartDate !== formatDateForInput(filters.startDate)) {
      onFiltersChange({
        ...filters,
        startDate: parseDateInput(debouncedStartDate),
      });
    }
  }, [debouncedStartDate]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (debouncedEndDate !== formatDateForInput(filters.endDate)) {
      onFiltersChange({
        ...filters,
        endDate: parseDateInput(debouncedEndDate),
      });
    }
  }, [debouncedEndDate]); // eslint-disable-line react-hooks/exhaustive-deps

  // Sync local state when filters prop changes externally (e.g., reset)
  useEffect(() => {
    setUserSearchInput(filters.userSearch || "");
    setStartDateInput(formatDateForInput(filters.startDate));
    setEndDateInput(formatDateForInput(filters.endDate));
  }, [filters.userSearch, filters.startDate, filters.endDate]);

  const handleFilterChange = (key: keyof AuditLogFiltersType, value: unknown) => {
    onFiltersChange({
      ...filters,
      [key]: value || undefined,
    });
  };

  const hasActiveFilters = Boolean(
    filters.userId ||
    filters.userSearch ||
    filters.action ||
    filters.targetEntity ||
    filters.startDate ||
    filters.endDate
  );

  return (
    <div className="space-y-4 p-4 border rounded-lg bg-muted/30">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4" aria-hidden="true" />
          <h3 className="text-sm font-semibold">{t("filters.title")}</h3>
        </div>
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onReset}
            className="h-8 text-xs"
            aria-label={t("filters.clearAll")}
          >
            <X className="h-3 w-3 mr-1" aria-hidden="true" />
            {t("filters.clearAll")}
          </Button>
        )}
      </div>

      <div className="flex gap-4 flex-wrap">
        <div className="space-y-2">
          <Label htmlFor="userSearch" className="text-xs">
            {t("filters.userSearch")}
          </Label>
          <div className="relative">
            <Input
              id="userSearch"
              type="text"
              placeholder={t("filters.userSearchPlaceholder")}
              value={userSearchInput}
              onChange={(e) => {
                setUserSearchInput(e.target.value);
                setIsUserSearchLoading(true);
              }}
              className="h-9 text-sm pr-8 min-w-72"
              aria-label={t("filters.userSearch")}
              aria-describedby={isUserSearchLoading ? "user-search-loading" : undefined}
            />
            {isUserSearchLoading && (
              <div
                id="user-search-loading"
                className="absolute right-2 top-1/2 -translate-y-1/2"
                aria-label={t("filters.userSearchLoading")}
              >
                <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
              </div>
            )}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="action" className="text-xs">
            {t("filters.action")}
          </Label>
          <Select
            value={filters.action || "all"}
            onValueChange={(value) =>
              handleFilterChange("action", value === "all" ? undefined : value)
            }
          >
            <SelectTrigger id="action" className="h-9 text-sm" aria-label={t("filters.action")}>
              <SelectValue placeholder={t("filters.allActions")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.allActions")}</SelectItem>
              {availableActions.map((action) => (
                <SelectItem key={action} value={action}>
                  {formatToTitleCase(action)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="targetEntity" className="text-xs">
            {t("filters.entity")}
          </Label>
          <Select
            value={filters.targetEntity || "all"}
            onValueChange={(value) =>
              handleFilterChange("targetEntity", value === "all" ? undefined : value)
            }
          >
            <SelectTrigger id="targetEntity" className="h-9 text-sm" aria-label={t("filters.entity")}>
              <SelectValue placeholder={t("filters.allEntities")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.allEntities")}</SelectItem>
              {availableEntities.map((entity) => (
                <SelectItem key={entity} value={entity}>
                  {formatToTitleCase(entity)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="startDate" className="text-xs">
            {t("filters.startDate")}
          </Label>
          <Input
            id="startDate"
            type="datetime-local"
            value={startDateInput}
            onChange={(e) => setStartDateInput(e.target.value)}
            className="h-9 text-sm"
            aria-label={t("filters.startDate")}
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="endDate" className="text-xs">
            {t("filters.endDate")}
          </Label>
          <Input
            id="endDate"
            type="datetime-local"
            value={endDateInput}
            onChange={(e) => setEndDateInput(e.target.value)}
            className="h-9 text-sm"
            aria-label={t("filters.endDate")}
          />
        </div>
      </div>
    </div>
  );
}

