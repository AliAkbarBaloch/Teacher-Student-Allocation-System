// react
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
// icons
import { Filter, Loader2, X } from "lucide-react";
// translations
import { useTranslation } from "react-i18next";
// hooks
import { apiClient } from "@/lib/api-client";
// services
import { TeacherService } from "@/features/teachers/services/teacherService";
// type
import type { Teacher } from "@/features/teachers/types/teacher.types";

interface AcademicYear {
  id: number;
  yearName: string;
}

interface TeacherFormSubmissionFiltersProps {
  yearId?: number;
  onYearIdChange: (value?: number) => void;
  teacherId?: number;
  onTeacherIdChange: (value?: number) => void;
  teacherSearch: string;
  onTeacherSearchChange: (value: string) => void;
  processedStatus?: "all" | "processed" | "unprocessed";
  onProcessedStatusChange: (value: "all" | "processed" | "unprocessed") => void;
  onReset: () => void;
  disabled?: boolean;
  searchLoading?: boolean;
}

const PROCESSED_STATUS_OPTIONS = [
  { value: "all", labelKey: "filters.statusAll" },
  { value: "processed", labelKey: "filters.statusProcessed" },
  { value: "unprocessed", labelKey: "filters.statusUnprocessed" },
] as const;

export function TeacherFormSubmissionFilters({
  yearId,
  onYearIdChange,
  teacherId,
  onTeacherIdChange,
  teacherSearch,
  onTeacherSearchChange,
  processedStatus = "all",
  onProcessedStatusChange,
  onReset,
  disabled = false,
  searchLoading = false,
}: TeacherFormSubmissionFiltersProps) {
  const { t } = useTranslation("teacherSubmissions");
  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [loadingYears, setLoadingYears] = useState(false);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [loadingTeachers, setLoadingTeachers] = useState(false);

  // Load academic years
  useEffect(() => {
    const loadAcademicYears = async () => {
      setLoadingYears(true);
      try {
        const response = await apiClient.get<{ success: boolean; data: AcademicYear[] }>(
          "/academic-years"
        );
        setAcademicYears(response.data || []);
      } catch (error) {
        console.error("Failed to load academic years:", error);
      } finally {
        setLoadingYears(false);
      }
    };
    loadAcademicYears();
  }, []);

  // Load teachers when search changes
  useEffect(() => {
    const loadTeachers = async () => {
      if (!teacherSearch.trim()) {
        setTeachers([]);
        return;
      }

      setLoadingTeachers(true);
      try {
        const response = await TeacherService.list({
          search: teacherSearch,
          page: 1,
          pageSize: 50,
          sortBy: "lastName",
          sortOrder: "asc",
        });
        setTeachers(response.items || []);
      } catch (error) {
        console.error("Failed to load teachers:", error);
        setTeachers([]);
      } finally {
        setLoadingTeachers(false);
      }
    };

    const timeoutId = setTimeout(loadTeachers, 300);
    return () => clearTimeout(timeoutId);
  }, [teacherSearch]);

  const hasActiveFilters = useMemo(() => {
    return (
      yearId !== undefined ||
      teacherId !== undefined ||
      processedStatus !== "all" ||
      teacherSearch.trim() !== ""
    );
  }, [yearId, teacherId, processedStatus, teacherSearch]);

  const statusOptions = useMemo(() => PROCESSED_STATUS_OPTIONS || [], []);

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
          <Label htmlFor="year-filter" className="text-xs">
            {t("filters.yearLabel")}
          </Label>
          <Select
            value={yearId ? String(yearId) : "all"}
            onValueChange={(value) => {
              if (value === "all") {
                onYearIdChange(undefined);
                return;
              }
              onYearIdChange(Number(value));
            }}
            disabled={disabled || loadingYears}
          >
            <SelectTrigger id="year-filter" className="h-9 text-sm">
              <SelectValue
                placeholder={loadingYears ? t("filters.loadingYears") : t("filters.yearPlaceholder")}
              />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.yearPlaceholder")}</SelectItem>
              {academicYears.map((year) => (
                <SelectItem key={year.id} value={String(year.id)}>
                  {year.yearName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="teacher-search" className="text-xs">
            {t("filters.teacherLabel")}
          </Label>
          <div className="relative">
            <Input
              id="teacher-search"
              placeholder={t("filters.teacherPlaceholder")}
              value={teacherSearch}
              disabled={disabled}
              onChange={(event) => onTeacherSearchChange(event.target.value)}
              className="h-9 text-sm w-full min-w-0 pr-8 md:min-w-72"
              aria-describedby={searchLoading || loadingTeachers ? "teacher-search-loading" : undefined}
            />
            {(searchLoading || loadingTeachers) && (
              <div
                id="teacher-search-loading"
                className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground"
                aria-hidden="true"
              >
                <Loader2 className="h-4 w-4 animate-spin" />
              </div>
            )}
          </div>
          {teacherSearch.trim() && teachers.length > 0 && !teacherId && (
            <div className="relative z-50">
              <div className="absolute mt-1 w-full md:min-w-72 bg-popover border rounded-md shadow-md max-h-60 overflow-auto">
                {teachers.map((teacher) => (
                  <button
                    key={teacher.id}
                    type="button"
                    className="w-full text-left px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground"
                    onClick={() => {
                      onTeacherIdChange(teacher.id);
                      onTeacherSearchChange(`${teacher.firstName} ${teacher.lastName}`);
                    }}
                  >
                    {teacher.firstName} {teacher.lastName} ({teacher.email})
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="processed-status-filter" className="text-xs">
            {t("filters.processedStatusLabel")}
          </Label>
          <Select
            value={processedStatus}
            onValueChange={(value) =>
              onProcessedStatusChange(value as "all" | "processed" | "unprocessed")
            }
            disabled={disabled}
          >
            <SelectTrigger id="processed-status-filter" className="h-9 text-sm">
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

