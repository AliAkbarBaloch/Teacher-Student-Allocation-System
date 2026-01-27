import React, { useEffect, useMemo, useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogAction,
  AlertDialogCancel,
} from "@/components/ui/alert-dialog";
import {
  DataTable,
  type ColumnConfig,
  type DataTableActions,
} from "@/components/common/DataTable";
import {
  fetchInternshipDemand,
  createInternshipDemand,
  updateInternshipDemand,
  deleteInternshipDemand,
} from "@/features/internship-demand/api";
import type {
  InternshipDemand,
  DemandFilter,
  DemandFormState,
  CreateInternshipDemandRequest,
} from "@/features/internship-demand/types";
import { InternshipDemandDialog } from "@/features/internship-demand/components/InternshipDemandDialog";
import { InternshipDemandFilters } from "@/features/internship-demand/components/InternshipDemandFilters";
import type { SchoolType } from "@/features/schools/types/school.types";
import { SCHOOL_TYPE_VALUES } from "@/features/schools/types/school.types";

// Use hooks for fetching data
import useAcademicYears from "@/hooks/entities/useAcademicYears";
import useSubjects from "@/hooks/entities/useSubjects";
import useInternshipTypes from "@/hooks/entities/useInternshipTypes";
// TODO, wire with real auth system. now it is like evryone is admin
const useIsAdmin = () => true;

const InternshipDemandPerYearPage: React.FC = () => {
  const isAdmin = useIsAdmin();

  // Use hooks for data
  const {
    data: academicYears = [],
    isLoading: isYearsLoading,
    error: yearsError,
  } = useAcademicYears();
  const {
    data: subjects = [],
    isLoading: isSubjectsLoading,
    error: subjectsError,
  } = useSubjects();
  const {
    data: internshipTypes = [],
    isLoading: isTypesLoading,
    error: typesError,
  } = useInternshipTypes();

  const [schoolTypes, setSchoolTypes] = useState<{ value: string; label: string }[]>([]);

  const [filters, setFilters] = useState<DemandFilter>({
    academicYearId: undefined,
    internshipTypeId: undefined,
    subjectId: undefined,
    schoolType: undefined,
    onlyForecasted: false,
  });

  const [data, setData] = useState<InternshipDemand[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Memos
  const academicYearById = useMemo(() => {
    const map = new Map<number, string>();
    academicYears.forEach((y) => map.set(y.id, y.yearName));
    return map;
  }, [academicYears]);

  const subjectById = useMemo(() => {
    const map = new Map<number, string>();
    subjects.forEach((s) => map.set(s.id, s.subjectTitle));
    return map;
  }, [subjects]);

  const internshipTypeById = useMemo(() => {
    const map = new Map<number, string>();
    internshipTypes.forEach((t) =>
      map.set(t.id, t.fullName || t.internshipCode)
    );
    return map;
  }, [internshipTypes]);

  const tableData = useMemo(() => {
    return data.map((row) => ({
      ...row,
      subject: subjectById.get(row.subjectId) ?? "",
    }));
  }, [data, subjectById]);

  const internshipDemandColumns: ColumnConfig[] = [
    {
      field: "academicYearId",
      title: "Academic Year",
      format: (value) => academicYearById.get(Number(value)) ?? String(value),
    },
    {
      field: "internshipTypeId",
      title: "Internship Type",
      format: (value) => internshipTypeById.get(Number(value)) ?? String(value),
    },
    {
      field: "schoolType",
      title: "School Type",
    },
    {
      field: "subjectId",
      title: "Subject",
      format: (value) => subjectById.get(Number(value)) ?? String(value),
    },
    {
      field: "requiredTeachers",
      title: "Required Teachers",
      align: "right",
      format: "number",
    },
    {
      field: "studentCount",
      title: "Student Count",
      align: "right",
      format: "number",
    },
    {
      field: "isForecasted",
      title: "Forecasted",
      format: (value) =>
        value ? (
          <Badge variant="success">Forecast</Badge>
        ) : (
          <Badge variant="outline">Official</Badge>
        ),
    },
    {
      field: "updatedAt",
      title: "Last Updated",
      format: "date",
    },
  ];

  // Load data from backend
  const loadData = useCallback(async () => {
    if (!filters.academicYearId) return;
    try {
      setLoading(true);
      setError(null);
      const list = await fetchInternshipDemand(filters);
      setData(Array.isArray(list) ? list : []);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to load internship demand");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  // autoload data when filters change
  useEffect(() => {
    loadData();
  }, [loadData]);

  // School types
  useEffect(() => {
    setSchoolTypes(
      SCHOOL_TYPE_VALUES.map((t) => ({
        value: t,
        label: t === "PRIMARY" ? "Primary school" : "Middle school",
      }))
    );
  }, []);

  // Dialog state
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<InternshipDemand | null>(null);
  const [form, setForm] = useState<DemandFormState>({
    academicYearId: "",
    subjectId: "",
    internshipTypeId: "",
    schoolType: "",
    requiredTeachers: "",
    studentCount: "",
    isForecasted: true,
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [backendError, setBackendError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<InternshipDemand | null>(null);
  const [deleting, setDeleting] = useState(false);

  // Open create dialog
  const openCreate = () => {
    setEditing(null);
    setForm({
      academicYearId: filters.academicYearId || "",
      subjectId: "",
      internshipTypeId: "",
      schoolType: "",
      requiredTeachers: "",
      studentCount: "",
      isForecasted: true,
    });
    setFormErrors({});
    setBackendError(null);
    setDialogOpen(true);
  };

  // Open edit dialog
  const openEdit = (row: InternshipDemand) => {
    setEditing(row);
    setForm({
      academicYearId: row.academicYearId ?? "",
      subjectId: row.subjectId ?? "",
      internshipTypeId: row.internshipTypeId ?? "",
      schoolType: row.schoolType,
      requiredTeachers: row.requiredTeachers,
      studentCount: row.studentCount,
      isForecasted: row.isForecasted,
    });
    setFormErrors({});
    setBackendError(null);
    setDialogOpen(true);
  };

  // Helper to update form fields
  type FormKey = keyof DemandFormState | "academicYearId" | "subjectId";
  const updateFormField = (field: FormKey, value: unknown) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  // Validation
  const validateForm = () => {
    const errs: Record<string, string> = {};
    if (!form.academicYearId)
      errs.academicYearId = "Academic year is required";
    if (!form.internshipTypeId)
      errs.internshipTypeId = "Internship type is required";
    if (!form.schoolType) errs.schoolType = "School type is required";
    if (!form.subjectId) errs.subjectId = "Subject is required";
    if (form.requiredTeachers === "" || form.requiredTeachers === null) {
      errs.requiredTeachers = "Required teachers is required";
    } else if (Number(form.requiredTeachers) < 0) {
      errs.requiredTeachers = "Required teachers must be ≥ 0";
    }
    if (form.studentCount === "" || form.studentCount === null) {
      errs.studentCount = "Student count is required";
    } else if (Number(form.studentCount) < 0) {
      errs.studentCount = "Student count must be ≥ 0";
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  // Save (create or update)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;
    const payload: CreateInternshipDemandRequest = {
      academicYearId: Number(form.academicYearId),
      subjectId: Number(form.subjectId),
      internshipTypeId: Number(form.internshipTypeId),
      schoolType: form.schoolType as SchoolType,
      requiredTeachers: Number(form.requiredTeachers),
      studentCount: Number(form.studentCount),
      isForecasted: Boolean(form.isForecasted),
    };
    try {
      setSubmitting(true);
      setBackendError(null);
      if (editing) {
        await updateInternshipDemand(String(editing.id), payload);
      } else {
        await createInternshipDemand(payload);
      }
      setDialogOpen(false);
      await loadData();
    } catch (err: unknown) {
      setBackendError(err instanceof Error ? err.message : "Failed to save internship demand");
    } finally {
      setSubmitting(false);
    }
  };

  // Delete
  const handleConfirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      setDeleting(true);
      await deleteInternshipDemand(String(deleteTarget.id));
      setDeleteTarget(null);
      await loadData();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Failed to delete internship demand");
    } finally {
      setDeleting(false);
    }
  };

  const tableActions: DataTableActions<InternshipDemand> | undefined = isAdmin
    ? {
        onEdit: (row) => openEdit(row),
        onDelete: (row) => setDeleteTarget(row),
      }
    : undefined;

  // Duplicate detection (not implemented)
  const duplicateExists = false;

  // Show loading or error for academic years/subjects/types
  const showLoading = isYearsLoading || isSubjectsLoading || isTypesLoading;
  const showError = yearsError || subjectsError || typesError;

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold tracking-tight">
          Internship Demand
        </h1>
        {isAdmin && (
          <Button size="sm" onClick={openCreate}>
            + Add demand
          </Button>
        )}
      </div>
      {/* Filters */}
      <Card>
        <InternshipDemandFilters
          filters={filters}
          onChange={setFilters}
          academicYears={academicYears}
          internshipTypes={internshipTypes}
          subjects={subjects}
          disabled={loading || showLoading}
          onReset={() =>
            setFilters((f) => ({
              ...f,
              internshipTypeId: undefined,
              subjectId: undefined,
              schoolType: undefined,
              onlyForecasted: false,
            }))
          }
        />
      </Card>

      {/* DataTable */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Demand list</CardTitle>
        </CardHeader>
        <CardContent>
          {showLoading ? (
            <div>Loading...</div>
          ) : showError ? (
            <div className="text-red-600">
              {yearsError?.toString() || subjectsError?.toString() || typesError?.toString()}
            </div>
          ) : (
            <DataTable<InternshipDemand>
              data={tableData}
              columnConfig={internshipDemandColumns}
              loading={loading}
              error={error}
              emptyMessage="No internship demand found."
              enableSearch={true}
              searchKey="subjectId"
              enablePagination={true}
              actions={tableActions}
              actionsHeader="Actions"
              disableInternalDialog={true}
              enableRowSelection={false}
              enableRowClick={false}
            />
          )}
        </CardContent>
      </Card>

      {/* Create/Edit Dialog */}
      <InternshipDemandDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        editing={!!editing}
        form={form}
        formErrors={formErrors}
        submitting={submitting}
        backendError={backendError}
        duplicateExists={duplicateExists}
        academicYears={academicYears}
        internshipTypes={internshipTypes}
        subjects={subjects}
        schoolTypes={schoolTypes}
        onChangeField={(field, value) => updateFormField(field as FormKey, value)}
        onSubmit={handleSubmit}
        onCancel={() => setDialogOpen(false)}
      />

      {/* Delete confirmation dialog */}
      <AlertDialog
        open={!!deleteTarget}
        onOpenChange={(open) => {
          if (!open) setDeleteTarget(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete this entry?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={deleting}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmDelete}
              disabled={deleting}
            >
              {deleting ? "Deleting…" : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default InternshipDemandPerYearPage;