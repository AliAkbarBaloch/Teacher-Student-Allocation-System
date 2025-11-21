import { useMemo } from "react";

// components
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";


// types
import type { SchoolType } from "../types/school.types";
import { createSchoolTypeOptions } from "../utils/schoolOptions";

// icons
import { Filter, Loader2, X } from "lucide-react";
// translations
import { useTranslation } from "react-i18next";

// constants
const STATUS_OPTIONS = [
  { value: "all", labelKey: "filters.statusAll" },
  { value: "active", labelKey: "filters.statusActive" },
  { value: "inactive", labelKey: "filters.statusInactive" },
] as const;

interface SchoolFiltersProps {
  searchValue: string;
  onSearchChange: (value: string) => void;
  schoolType?: SchoolType;
  onSchoolTypeChange: (value?: SchoolType) => void;
  zoneNumber?: string;
  onZoneNumberChange: (value?: string) => void;
  status?: "all" | "active" | "inactive";
  onStatusChange: (value: "all" | "active" | "inactive") => void;
  onReset: () => void;
  disabled?: boolean;
  searchLoading?: boolean;
}

export function SchoolFilters({
  searchValue,
  onSearchChange,
  schoolType,
  onSchoolTypeChange,
  zoneNumber,
  onZoneNumberChange,
  status = "all",
  onStatusChange,
  onReset,
  disabled = false,
  searchLoading = false,
}: SchoolFiltersProps) {
  const { t } = useTranslation("schools");

  const schoolTypeOptions = useMemo(() => createSchoolTypeOptions(t), [t]);

  const typeSelectValue = schoolType ?? "all";
  const hasActiveFilters =
    Boolean(searchValue?.trim()) ||
    Boolean(schoolType) ||
    Boolean(zoneNumber) ||
    status !== "all";

  return (
    <div className="space-y-4 p-4 border rounded-lg bg-muted/30">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
          <div>
            <h3 className="text-sm font-semibold">{t("filters.title")}</h3>
            <p className="text-xs text-muted-foreground">{t("filters.subtitle")}</p>
          </div>
        </div>
        {hasActiveFilters && (
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={onReset}
            disabled={disabled}
            className="h-8 text-xs"
          >
            <X className="h-3.5 w-3.5 mr-1" aria-hidden="true" />
            {t("filters.reset")}
          </Button>
        )}
      </div>

      <div className="flex flex-wrap gap-4">
        <div className="space-y-2">
          <Label htmlFor="school-search" className="text-xs">
            {t("filters.searchLabel")}
          </Label>
          <div className="relative">
            <Input
              id="school-search"
              placeholder={t("filters.searchPlaceholder")}
              value={searchValue}
              disabled={disabled}
              onChange={(event) => onSearchChange(event.target.value)}
              className="h-9 text-sm w-full min-w-0 pr-8 md:min-w-72"
              aria-describedby={searchLoading ? "school-search-loading" : undefined}
            />
            {searchLoading && (
              <div
                id="school-search-loading"
                className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground"
                aria-hidden="true"
              >
                <Loader2 className="h-4 w-4 animate-spin" />
              </div>
            )}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="school-type" className="text-xs">
            {t("filters.typeLabel")}
          </Label>
          <Select
            value={typeSelectValue}
            onValueChange={(value) => {
              if (value === "all") {
                onSchoolTypeChange(undefined);
                return;
              }
              onSchoolTypeChange(value as SchoolType);
            }}
            disabled={disabled}
          >
            <SelectTrigger id="school-type" className="h-9 text-sm">
              <SelectValue placeholder={t("filters.typePlaceholder")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.typePlaceholder")}</SelectItem>
              {schoolTypeOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="zone-filter" className="text-xs">
            {t("filters.zoneLabel")}
          </Label>
          <Input
            id="zone-filter"
            type="number"
            min={1}
            step={1}
            placeholder={t("filters.zonePlaceholder")}
            value={zoneNumber ?? ""}
            disabled={disabled}
            onChange={(event) => onZoneNumberChange(event.target.value || undefined)}
            className="h-9 text-sm"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="status-filter" className="text-xs">
            {t("filters.statusLabel")}
          </Label>
          <Select
            value={status}
            onValueChange={(value) => onStatusChange(value as "all" | "active" | "inactive")}
            disabled={disabled}
          >
            <SelectTrigger id="status-filter" className="h-9 text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {t(option.labelKey)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  );
}

