import { useMemo } from "react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

import { Checkbox } from "@/components/ui/checkbox";

//icons 
import { Filter, X } from "lucide-react";

//types 
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";

import type { DemandFilter } from "@/features/internship-demand/types";


//reuse SchoolType
import {
    SCHOOL_TYPE_VALUES,
    type SchoolType,

} from "@/features/schools/types/school.types";

//component props (what this component recievies)
// it recieves : 
// filter - current filter values 
// onChange - a function to tell the parent "filters changed"
interface InternshipDemandFiltersProps {
    filters: DemandFilter;
    onChange: (next: DemandFilter) => void;

    academicYears: AcademicYear[];
    internshipTypes: InternshipType[];
    subjects: Subject[];

    onReset: () => void;
    disabled: boolean;
}

export function InternshipDemandFilters({
    filters,
    onChange,
    academicYears,
    internshipTypes,
    subjects,
    onReset,
    disabled = false,
}: InternshipDemandFiltersProps) {
    //build options 
    const schoolTypeOptions = useMemo(
        () => SCHOOL_TYPE_VALUES.map((v) => ({ value: v, label: v })),
        []
    );

    //  use "all" sentinel
    const yearSelectValue = filters.academicYearId ? String(filters.academicYearId) : "";
    const internshipTypeSelectValue = filters.internshipTypeId ? String(filters.internshipTypeId) : "all";
    const subjectSelectValue = filters.subjectId ? String(filters.subjectId) : "all";
    const schoolTypeSelectValue = filters.schoolType ?? "all";

    //  show Reset only when something besides year is active
    const hasActiveFilters =
        Boolean(filters.internshipTypeId) ||
        Boolean(filters.subjectId) ||
        Boolean(filters.schoolType) ||
        Boolean(filters.onlyForecasted);

    return (
        <div className="space-y-4 p-4 border rounded-lg bg-muted/30">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
                    <div>
                        <h3 className="text-sm font-semibold">Filters</h3>
                        <p className="text-xs text-muted-foreground">
                            Narrow down the demand list
                        </p>
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
                        Reset
                    </Button>
                )}
            </div>

            <div className="flex flex-wrap gap-4">
                {/* Academic Year */}
                <div className="space-y-2">
                    <Label htmlFor="demand-year" className="text-xs">
                        Academic year *
                    </Label>
                    <Select
                        value={yearSelectValue}
                        onValueChange={(value) =>
                            onChange({ ...filters, academicYearId: Number(value) })
                        }
                        disabled={disabled}
                    >
                        <SelectTrigger id="demand-year" className="h-9 text-sm">
                            <SelectValue placeholder="Select year" />
                        </SelectTrigger>
                        <SelectContent>
                            {academicYears.map((y) => (
                                <SelectItem key={y.id} value={String(y.id)}>
                                    {y.yearName}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                {/* Internship Type */}
                <div className="space-y-2">
                    <Label htmlFor="demand-internship-type" className="text-xs">
                        Internship type
                    </Label>
                    <Select
                        value={internshipTypeSelectValue}
                        onValueChange={(value) => {
                            if (value === "all") {
                                onChange({ ...filters, internshipTypeId: "" });
                                return;
                            }
                            onChange({ ...filters, internshipTypeId: Number(value) });
                        }}
                        disabled={disabled}
                    >
                        <SelectTrigger id="demand-internship-type" className="h-9 text-sm">
                            <SelectValue placeholder="All internship types" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">All internship types</SelectItem>
                            {internshipTypes.map((t) => (
                                <SelectItem key={t.id} value={String(t.id)}>
                                    {t.fullName ?? t.internshipCode ?? `Type #${t.id}`}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                {/* School Type */}
                <div className="space-y-2">
                    <Label htmlFor="demand-school-type" className="text-xs">
                        School type
                    </Label>
                    <Select
                        value={schoolTypeSelectValue}
                        onValueChange={(value) => {
                            if (value === "all") {
                                onChange({ ...filters, schoolType: undefined });
                                return;
                            }
                            onChange({ ...filters, schoolType: value as SchoolType });
                        }}
                        disabled={disabled}
                    >
                        <SelectTrigger id="demand-school-type" className="h-9 text-sm">
                            <SelectValue placeholder="All school types" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">All school types</SelectItem>
                            {schoolTypeOptions.map((opt) => (
                                <SelectItem key={opt.value} value={opt.value}>
                                    {opt.label}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                {/* Subject */}
                <div className="space-y-2">
                    <Label htmlFor="demand-subject" className="text-xs">
                        Subject
                    </Label>
                    <Select
                        value={subjectSelectValue}
                        onValueChange={(value) => {
                            if (value === "all") {
                                onChange({ ...filters, subjectId: "" });
                                return;
                            }
                            onChange({ ...filters, subjectId: Number(value) });
                        }}
                        disabled={disabled}
                    >
                        <SelectTrigger id="demand-subject" className="h-9 text-sm">
                            <SelectValue placeholder="All subjects" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">All subjects</SelectItem>
                            {subjects.map((s) => (
                                <SelectItem key={s.id} value={String(s.id)}>
                                    {s.subjectTitle}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>

                {/* Only forecasted */}
                <div className="flex items-end gap-2">
                    <Checkbox
                        id="demand-only-forecasted"
                        checked={Boolean(filters.onlyForecasted)}
                        onCheckedChange={(checked) =>
                            onChange({ ...filters, onlyForecasted: Boolean(checked) })
                        }
                        disabled={disabled}
                    />
                    <Label htmlFor="demand-only-forecasted" className="text-xs">
                        Show only forecasted
                    </Label>
                </div>
            </div>
        </div>
    );
}






