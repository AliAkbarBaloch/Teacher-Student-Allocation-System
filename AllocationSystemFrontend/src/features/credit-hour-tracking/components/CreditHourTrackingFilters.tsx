// react
import { useEffect, useState, useMemo, useRef } from "react";
// components
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
// icons
import { Filter, Loader2, X, Search } from "lucide-react";
// translations
import { useTranslation } from "react-i18next";
// hooks
// services
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";
import { TeacherService } from "@/features/teachers/services/teacherService";
// types
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import type { Teacher } from "@/features/teachers/types/teacher.types";
import { SelectField } from "@/components/form/fields/SelectField";

interface CreditHourTrackingFiltersProps {
  yearId?: number;
  onYearIdChange: (value?: number) => void;
  teacherId?: number;
  onTeacherIdChange: (value?: number) => void;
  teacherSearch: string;
  onTeacherSearchChange: (value: string) => void;
  minBalance?: number;
  onMinBalanceChange: (value?: number) => void;
  onReset: () => void;
  disabled?: boolean;
}

export function CreditHourTrackingFilters({
  yearId,
  onYearIdChange,
  teacherId,
  onTeacherIdChange,
  teacherSearch,
  onTeacherSearchChange,
  minBalance,
  onMinBalanceChange,
  onReset,
  disabled = false,
}: CreditHourTrackingFiltersProps) {
  const { t } = useTranslation("creditHourTracking");
  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [loadingYears, setLoadingYears] = useState(false);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [loadingTeachers, setLoadingTeachers] = useState(false);
  const [showTeacherDropdown, setShowTeacherDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Load academic years
  useEffect(() => {
    let isMounted = true;
    const loadAcademicYears = async () => {
      setLoadingYears(true);
      try {
        const years = await AcademicYearService.getAll();
        if (isMounted) {
          setAcademicYears(years);
        }
      } catch (error) {
        console.error("Failed to load academic years:", error);
        if (isMounted) {
          setAcademicYears([]);
        }
      } finally {
        if (isMounted) {
          setLoadingYears(false);
        }
      }
    };
    loadAcademicYears();
    
    return () => {
      isMounted = false;
    };
  }, []);

  // Load teachers when search changes (with debounce and cancellation)
  useEffect(() => {
    let isMounted = true;
    let timeoutId: ReturnType<typeof setTimeout> | null = null;
    
    const loadTeachers = async () => {
      if (!teacherSearch.trim()) {
        if (isMounted) {
          setTeachers([]);
        }
        return;
      }

      if (isMounted) {
        setLoadingTeachers(true);
      }
      
      try {
        const response = await TeacherService.list({
          search: teacherSearch,
          page: 1,
          pageSize: 50,
          sortBy: "lastName",
          sortOrder: "asc",
        });
        if (isMounted) {
          setTeachers(response.items || []);
        }
      } catch (error) {
        console.error("Failed to load teachers:", error);
        if (isMounted) {
          setTeachers([]);
        }
      } finally {
        if (isMounted) {
          setLoadingTeachers(false);
        }
      }
    };

    timeoutId = setTimeout(loadTeachers, 300);
    
    return () => {
      isMounted = false;
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, [teacherSearch]);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        inputRef.current &&
        !inputRef.current.contains(event.target as Node)
      ) {
        setShowTeacherDropdown(false);
      }
    };

    if (showTeacherDropdown) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }
  }, [showTeacherDropdown]);

  // Show dropdown when teachers are loaded
  useEffect(() => {
    if (teachers.length > 0 && teacherSearch.trim() && !teacherId) {
      setShowTeacherDropdown(true);
    } else {
      setShowTeacherDropdown(false);
    }
  }, [teachers, teacherSearch, teacherId]);

  const hasActiveFilters = useMemo(() => {
    return (
      yearId !== undefined ||
      teacherId !== undefined ||
      minBalance !== undefined ||
      teacherSearch.trim() !== ""
    );
  }, [yearId, teacherId, minBalance, teacherSearch]);

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

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">

          <SelectField
            id="year-filter"
            label={t("filters.yearLabel")}
            options={[
              { value: "all", label: t("filters.yearPlaceholder") },
              ...academicYears.map((year) => ({
                label: year.yearName,
                value: String(year.id),
              })),
            ]}
            value={yearId ? String(yearId) : "all"}
            onChange={(value) => {
              if (value === "all") {
                onYearIdChange(undefined);
                return;
              }
              onYearIdChange(Number(value));
            }}
            disabled={disabled || loadingYears}
            placeholder={loadingYears ? t("filters.loadingYears") : t("filters.yearPlaceholder")}
          />


        <div className="space-y-2">
          <Label htmlFor="teacher-search" className="text-sm font-medium">
            {t("filters.teacherLabel")}
          </Label>
          <div className="relative">
            <Search className="absolute left-2 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none z-10" />
            <Input
              ref={inputRef}
              id="teacher-search"
              placeholder={t("filters.teacherPlaceholder")}
              value={teacherSearch}
              disabled={disabled}
              onChange={(event) => onTeacherSearchChange(event.target.value)}
              onFocus={() => {
                if (teachers.length > 0 && teacherSearch.trim() && !teacherId) {
                  setShowTeacherDropdown(true);
                }
              }}
              className={`h-9 text-sm w-full pl-8 ${teacherId || teacherSearch.trim() ? "pr-8" : "pr-2"}`}
              aria-describedby={loadingTeachers ? "teacher-search-loading" : undefined}
              aria-expanded={showTeacherDropdown}
              aria-haspopup="listbox"
            />
            {loadingTeachers ? (
              <div
                id="teacher-search-loading"
                className="absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground z-10"
                aria-hidden="true"
              >
                <Loader2 className="h-4 w-4 animate-spin" />
              </div>
            ) : (teacherId || teacherSearch.trim()) && (
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onTeacherIdChange(undefined);
                  onTeacherSearchChange("");
                  inputRef.current?.focus();
                }}
                className="absolute right-2 top-1/2 -translate-y-1/2 h-5 w-5 flex items-center justify-center rounded-sm hover:bg-accent text-muted-foreground hover:text-foreground transition-colors z-10"
                aria-label="Clear teacher filter"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
          {showTeacherDropdown && teachers.length > 0 && (
            <div ref={dropdownRef} className="relative z-50" role="listbox">
              <div className="absolute mt-1 w-full bg-popover border rounded-md shadow-md max-h-60 overflow-auto">
                {teachers.map((teacher) => (
                  <button
                    key={teacher.id}
                    type="button"
                    role="option"
                    className="w-full text-left px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:outline-none"
                    onClick={() => {
                      onTeacherIdChange(teacher.id);
                      onTeacherSearchChange(`${teacher.firstName} ${teacher.lastName}`);
                      setShowTeacherDropdown(false);
                    }}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" || e.key === " ") {
                        e.preventDefault();
                        onTeacherIdChange(teacher.id);
                        onTeacherSearchChange(`${teacher.firstName} ${teacher.lastName}`);
                        setShowTeacherDropdown(false);
                      } else if (e.key === "Escape") {
                        setShowTeacherDropdown(false);
                        inputRef.current?.focus();
                      }
                    }}
                    tabIndex={0}
                  >
                    {teacher.firstName} {teacher.lastName} ({teacher.email})
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="min-balance" className="text-sm font-medium">
            {t("filters.minBalanceLabel")}
          </Label>
          <div className="relative">
            <Search className="absolute left-2 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none z-10" />
            <Input
              id="min-balance"
              type="number"
              step="0.1"
              placeholder={t("filters.minBalancePlaceholder")}
              value={minBalance ?? ""}
              disabled={disabled}
              onChange={(e) => {
                const value = e.target.value;
                if (value === "") {
                  onMinBalanceChange(undefined);
                  return;
                }
                const numValue = Number(value);
                if (!isNaN(numValue)) {
                  onMinBalanceChange(numValue);
                }
              }}
              className={`h-9 text-sm w-full pl-8 ${minBalance !== undefined ? "pr-8" : "pr-2"}`}
            />
            {minBalance !== undefined && (
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onMinBalanceChange(undefined);
                }}
                className="absolute right-2 top-1/2 -translate-y-1/2 h-5 w-5 flex items-center justify-center rounded-sm hover:bg-accent text-muted-foreground hover:text-foreground transition-colors z-10"
                aria-label="Clear min balance filter"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
