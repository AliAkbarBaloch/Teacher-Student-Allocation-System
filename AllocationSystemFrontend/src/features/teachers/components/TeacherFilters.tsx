import { useEffect, useState, useMemo } from "react";

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
import type { EmploymentStatus } from "../types/teacher.types";
import { SchoolService } from "@/features/schools/services/schoolService";
import type { School } from "@/features/schools/types/school.types";
import { EMPLOYMENT_STATUS_OPTIONS } from "@/lib/constants/teachers";

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

interface TeacherFiltersProps {
  searchValue: string;
  onSearchChange: (value: string) => void;
  schoolId?: number;
  onSchoolIdChange: (value?: number) => void;
  employmentStatus?: EmploymentStatus;
  onEmploymentStatusChange: (value?: EmploymentStatus) => void;
  status?: "all" | "active" | "inactive";
  onStatusChange: (value: "all" | "active" | "inactive") => void;
  onReset: () => void;
  disabled?: boolean;
  searchLoading?: boolean;
}

export function TeacherFilters({
  searchValue,
  onSearchChange,
  schoolId,
  onSchoolIdChange,
  employmentStatus,
  onEmploymentStatusChange,
  status = "all",
  onStatusChange,
  onReset,
  disabled = false,
  searchLoading = false,
}: TeacherFiltersProps) {
  const { t } = useTranslation("teachers");
  const [schools, setSchools] = useState<School[]>([]);
  const [loadingSchools, setLoadingSchools] = useState(false);

  // Ensure options arrays are always defined
  const statusOptions = useMemo(() => STATUS_OPTIONS || [], []);
  const employmentStatusOptions = useMemo(() => EMPLOYMENT_STATUS_OPTIONS || [], []);

  useEffect(() => {
    const loadSchools = async () => {
      setLoadingSchools(true);
      try {
        const response = await SchoolService.list({
          isActive: true,
          page: 1,
          pageSize: 1000,
          sortBy: "schoolName",
          sortOrder: "asc",
        });
        setSchools(response.items);
      } catch (error) {
        console.error("Failed to load schools:", error);
      } finally {
        setLoadingSchools(false);
      }
    };
    loadSchools();
  }, []);

  const hasActiveFilters =
    Boolean(searchValue?.trim()) ||
    Boolean(schoolId) ||
    Boolean(employmentStatus) ||
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
          <Label htmlFor="teacher-search" className="text-xs">
            {t("filters.searchLabel")}
          </Label>
          <div className="relative">
            <Input
              id="teacher-search"
              placeholder={t("filters.searchPlaceholder")}
              value={searchValue}
              disabled={disabled}
              onChange={(event) => onSearchChange(event.target.value)}
              className="h-9 text-sm w-full min-w-0 pr-8 md:min-w-72"
              aria-describedby={searchLoading ? "teacher-search-loading" : undefined}
            />
            {searchLoading && (
              <div
                id="teacher-search-loading"
                className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground"
                aria-hidden="true"
              >
                <Loader2 className="h-4 w-4 animate-spin" />
              </div>
            )}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="school-filter" className="text-xs">
            {t("filters.schoolLabel")}
          </Label>
          <Select
            value={schoolId ? String(schoolId) : "all"}
            onValueChange={(value) => {
              if (value === "all") {
                onSchoolIdChange(undefined);
                return;
              }
              onSchoolIdChange(Number(value));
            }}
            disabled={disabled || loadingSchools}
          >
            <SelectTrigger id="school-filter" className="h-9 text-sm">
              <SelectValue placeholder={loadingSchools ? t("filters.loadingSchools") : t("filters.schoolPlaceholder")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.schoolPlaceholder")}</SelectItem>
              {(schools || []).map((school) => (
                <SelectItem key={school.id} value={String(school.id)}>
                  {school.schoolName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="employment-status-filter" className="text-xs">
            {t("filters.employmentStatusLabel")}
          </Label>
          <Select
            value={employmentStatus || "all"}
            onValueChange={(value) => {
              if (value === "all") {
                onEmploymentStatusChange(undefined);
                return;
              }
              onEmploymentStatusChange(value as EmploymentStatus);
            }}
            disabled={disabled}
          >
            <SelectTrigger id="employment-status-filter" className="h-9 text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.employmentStatusPlaceholder")}</SelectItem>
              {employmentStatusOptions.map((status) => (
                <SelectItem key={status} value={status}>
                  {t(`form.employmentStatus.${status}`)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
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
              {statusOptions.map((option) => (
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

