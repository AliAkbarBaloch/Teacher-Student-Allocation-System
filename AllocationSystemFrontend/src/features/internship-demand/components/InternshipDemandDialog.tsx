import React from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
    Dialog,
    DialogBody,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";

import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { DemandFormState } from "@/features/internship-demand/types";

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;

    editing: boolean;

    form: DemandFormState;
    formErrors: Record<string, string>;
    submitting: boolean;

    backendError: string | null;
    duplicateExists: boolean;

    academicYears: AcademicYear[];
    internshipTypes: InternshipType[];
    subjects: Subject[];
    schoolTypes: { value: string; label: string }[];

    onChangeField: (field: keyof DemandFormState, value: unknown) => void;
    onSubmit: (e: React.FormEvent) => void;
    onCancel: () => void;
};

export function InternshipDemandDialog({
    open,
    onOpenChange,
    editing,
    form,
    formErrors,
    submitting,
    backendError,
    duplicateExists,
    academicYears,
    internshipTypes,
    subjects,
    schoolTypes,
    onChangeField,
    onSubmit,
    onCancel,
}: Props) {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="max-w-3xl">
                <DialogHeader>
                    <DialogTitle>
                        {editing ? "Edit internship demand" : "Create internship demand"}
                    </DialogTitle>
                    <DialogDescription>
                        Fill in the details and save the internship demand.
                    </DialogDescription>
                </DialogHeader>

                <DialogBody>
                    <form onSubmit={onSubmit} className="space-y-4">
                        <div className="grid gap-4 md:grid-cols-2">
                            {/* Academic year */}
                            <div className="space-y-1">
                                <Label>Academic year *</Label>
                                <Select
                                    value={String(form.academicYearId || "")}
                                    onValueChange={(value) =>
                                        onChangeField("academicYearId", Number(value))
                                    }
                                >
                                    <SelectTrigger className="h-10 w-full">
                                        <SelectValue placeholder="Select academic year" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {academicYears.map((y) => (
                                            <SelectItem key={y.id} value={String(y.id)}>
                                                {y.yearName}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                {formErrors.academicYearId && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.academicYearId}
                                    </p>
                                )}
                            </div>

                            {/* Internship type */}
                            <div className="space-y-1">
                                <Label>Internship type *</Label>
                                <Select
                                    value={String(form.internshipTypeId || "")}
                                    onValueChange={(value) =>
                                        onChangeField("internshipTypeId", Number(value))
                                    }
                                >
                                    <SelectTrigger className="h-10 w-full">
                                        <SelectValue placeholder="Select internship type" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {internshipTypes.map((t) => (
                                            <SelectItem key={t.id} value={String(t.id)}>
                                                {t.fullName ?? t.internshipCode ?? `Type #${t.id}`}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                {formErrors.internshipTypeId && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.internshipTypeId}
                                    </p>
                                )}
                            </div>

                            {/* School type */}
                            <div className="space-y-1">
                                <Label>School type *</Label>
                                <Select
                                    value={form.schoolType || ""}
                                    onValueChange={(value) => onChangeField("schoolType", value)}
                                >
                                    <SelectTrigger className="h-10 w-full">
                                        <SelectValue placeholder="Select school type" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {schoolTypes.map((st) => (
                                            <SelectItem key={st.value} value={st.value}>
                                                {st.label}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                {formErrors.schoolType && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.schoolType}
                                    </p>
                                )}
                            </div>

                            {/* Subject */}
                            <div className="space-y-1">
                                <Label>Subject *</Label>
                                <Select
                                    value={form.subjectId ? String(form.subjectId) : ""}
                                    onValueChange={(value) =>
                                        onChangeField("subjectId", Number(value))
                                    }
                                >
                                    <SelectTrigger className="h-10 w-full">
                                        <SelectValue placeholder="Select subject" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {subjects.map((s) => (
                                            <SelectItem key={s.id} value={String(s.id)}>
                                                {s.subjectTitle}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                {formErrors.subjectId && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.subjectId}
                                    </p>
                                )}
                            </div>
                        </div>

                        <div className="grid gap-4 md:grid-cols-2">
                            {/* Required teachers */}
                            <div className="space-y-1">
                                <Label htmlFor="form-requiredTeachers">Required teachers *</Label>
                                <Input
                                    className="h-10 w-full"
                                    id="form-requiredTeachers"
                                    type="number"
                                    min={0}
                                    value={form.requiredTeachers === "" ? "" : String(form.requiredTeachers)}
                                    onChange={(e) =>
                                        onChangeField(
                                            "requiredTeachers",
                                            e.target.value === "" ? "" : Number(e.target.value)
                                        )
                                    }
                                />
                                {formErrors.requiredTeachers && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.requiredTeachers}
                                    </p>
                                )}
                            </div>

                            {/* Student count */}
                            <div className="space-y-1">
                                <Label htmlFor="form-studentCount">Student count *</Label>
                                <Input
                                    className="h-10 w-full"
                                    id="form-studentCount"
                                    type="number"
                                    min={0}
                                    value={form.studentCount}
                                    onChange={(e) =>
                                        onChangeField(
                                            "studentCount",
                                            e.target.value === "" ? "" : Number(e.target.value)
                                        )
                                    }
                                />
                                {formErrors.studentCount && (
                                    <p className="text-xs text-destructive">
                                        {formErrors.studentCount}
                                    </p>
                                )}
                            </div>
                        </div>

                        {/* Forecasted */}
                        <div className="flex items-center gap-2">
                            <Checkbox
                                id="form-isForecasted"
                                checked={form.isForecasted}
                                onCheckedChange={(checked) =>
                                    onChangeField("isForecasted", Boolean(checked))
                                }
                            />
                            <Label htmlFor="form-isForecasted" className="text-xs">
                                Is forecasted
                            </Label>
                        </div>

                        {duplicateExists && (
                            <div className="rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-xs text-amber-800">
                                An entry with the same year, internship type, school type and
                                subject already exists. Saving may create a duplicate.
                            </div>
                        )}

                        {backendError && (
                            <div className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                                {backendError}
                            </div>
                        )}

                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                onClick={onCancel}
                                disabled={submitting}
                            >
                                Cancel
                            </Button>
                            <Button type="submit" disabled={submitting}>
                                {submitting ? "Saving…" : "Save"}
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogBody>
            </DialogContent>
        </Dialog>
    );
}
