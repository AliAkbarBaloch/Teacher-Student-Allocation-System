/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable @typescript-eslint/no-explicit-any */
//
import React from "react";
// useState - remember values 
// useEffect - runs code when something changes 
// useMemo - remember a calculated result 
import {useEffect, useMemo, useState} from "react";

//styled button 
import {Button} from "@/components/ui/button";
//Card - box, CardHeader, CardTitle, CardContent - parts of that box
import {Card, CardHeader, CardTitle, CardContent} from  "@/components/ui/card";
// text/number field 
import {Input} from "@/components/ui/input";
// Label - text label for inputs 
import {Label} from "@/components/ui/label";
//Chechbox input 
import {Checkbox} from "@/components/ui/checkbox";
//small pill label for status 
import {Badge} from "@/components/ui/badge";

//components for a popup window 
import {
    // Wrapper for the model 
    Dialog, 
    // Main box inside the modal 
    DialogContent,
    // sections of the modal 
    DialogHeader, 
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";

//components for a confirmation popup 
import {
    //wrapper 
    AlertDialog, 
    //pieces of that popup 
    AlertDialogContent, 
    //
    AlertDialogHeader, 
    AlertDialogTitle, 
    AlertDialogDescription, 
    AlertDialogFooter,
    // confirm button 
    AlertDialogAction,
    //cancel button 
    AlertDialogCancel,
} from "@/components/ui/alert-dialog";

import {
    // the main table UI 
    DataTable, 
    // type that describes each column (name, type, ... )
    type ColumnConfig, 
    // type that describes actions (edit/delete) for each button
    type DataTableActions, 
} from "@/components/common/DataTable";

import {
    // get list 
    fetchInternshipDemand, 
    // create new item 
    createInternshipDemand,
    //update existing 
    updateInternshipDemand,
    //remove item 
    deleteInternshipDemand,
} from "@/features/internship-demand/api";

import type {
    // what one row looks like 
    InternshipDemand,
    // what the filter look like 
    DemandFilter, 
    // what the form values look like 
    DemandFormState,
} from "@/features/internship-demand/types";


// current year 
const defaultYear = new Date().getFullYear();

// TODO, wire with real auth system. now it is like evryone is admin
const useIsAdmin = () => true; 

// An array of column configs 
// TypeScript: this is an array of ColumnConfig objects 
// Table column configuration for the generic DataTable 
const internshipDemandColumns : ColumnConfig[] = [
    {
        // column year 
        field: "year", 
        // text shown at the top of the column 
        title: "Year",
        //text alignment in cell 
        align: "left",
        //if used in forms, this is a number field 
        fieldType: "number",
        // this filed is required in the form 
        fieldRequired: true,
    },
    {
        //column internshipType 
        field: "internshipType",
        title: "Internship Type",
        fieldType: "text",
        fieldRequired: true,

    },
    {
        // column schoolType 
        field: "schoolType",
        title: "School Type",
        fieldType: "text",
        fieldRequired: true, 
    },
    {
        //column subject 
        field: "subject",
        title: "Subject",
        fieldType: "text",
        fieldRequired: true, 
    },
    {
        //column requredTeacher 
        field:"requiredTeachers",
        title: "Required Teachers",
        align: "right",
        fieldType: "number",
        fieldRequired: true,
    },
    {
        //column studentCount 
        field : "studentCount",
        title: "Student Count",
        align: "right",
        format: "number",
        fieldType: "number",
        fieldRequired: true,

    },
    {
        // column for forecasted (true/false)
        field: "forecasted", 
        title: "Forecasted",
        // Optional your DataTableBody can use this formatter if implemented
        // value - what is in this field 
        format: (value: unknown) => 
        {
            const v = Boolean(value);
            return v ? (<Badge variant="success"> Forecast </Badge>) : (
            <Badge variant="outline"> Official </Badge>);
        },

    },
    {//column for updatedAt 
        field: "updatedAt",
        title: "Last Updated",
        format: "date",
    },
    
];

// main component definition 

// React.FC - react functional component 
const InternshipDemandPerYearPage: React.FC = () => {

    // evryone is admin for now. to decide whether show buttons like Add/Edit/Delete 
    const isAdmin = useIsAdmin(); 

    //filter state (searh control)

    // useState - creates a piece of memory, 
    // filters - current filters,
    //  setFilters - function to change them. 
    // Initial filter values year, other fileds are empty, onlyForecasted = false  
    const [filters, setFilters] = useState<DemandFilter>(
        {
            year: defaultYear, 
            internshipType:"",
            schoolType:"",
            subject: "",
            onlyForecasted: false,
        });

    // data loading state (what we got from backend)

    //data - array of InternshipDemand, setData - function to update that array, array starts as empty  

    const [data, setData] = useState<InternshipDemand[]>([]);

    //loading - true/false; true - when loading data from backend 
    const [loading, setLoading] = useState(false);

    // error - error message string or null 
    // used to show errors above the table 
    const [error, setError] = useState<string|null>(null);

    //function to load data from backend 
    const loadData = async () => {

        //if year is empty - do nothing 
        if (!filters.year) return; 

        try {

            //show loading spinner
            setLoading(true);
            //clear any old error 
            setError(null);

            //calls backend with current filters 
            //await - wait for response, list - array returned from backend, setDate(list) - update data with new list 
            const list = await fetchInternshipDemand(filters);

            console.log("internship demands response", list);

            //update data with new list 
            setData(Array.isArray(list)? list: []);
        
        } catch (e: any) 
        {
            //if somethng goes wrongs set an error message 
            setError(e?.message ?? "Failed to load internship demand")
        }
        finally 
        {
            //stop spinner
            setLoading(false);
        }

        

    };

    // autoload data when filters change 
    // useEffect runs after render and whenever dependencies change 
    // whenenver any filter changes -> run loadData(). the array after the comma - lists those dependencies 
    useEffect( ()=>
    {
        loadData();
    }, [
        filters.year,
        filters.internshipType,
        filters.schoolType,
        filters.subject,
        filters.onlyForecasted,
        ]
    );
    
    //Dialog state - Create/Edit popup

    //dialogOpen - weather the create/edit dialog is open. Starts as false(closed). 
    const [dialogOpen, setDialogOpen] = useState(false);
    
    // editing - which row we are editing, null - we are creating new one, if set to a row - editing mode 
    const [editing, setEditing] = useState<InternshipDemand | null >(null);

    //form - values currently in the dialog box form. setForm - function to change them 
    //starting values : empty form, year = current year, forecaseted = true 

    const [form, setForm] = useState<DemandFormState>({
        year: defaultYear,
        internshipType: "",
        schoolType: "",
        subject: "",
        requiredTeachers:"",
        studentCount:"",
        forecasted: true,
    });

    // formErrors are objects like {year: "Year is required..."}
    // setFormErrors - update validation error message 
    const [formErrors, setFormErrors] = useState<Record<string,string>>({});

    //backendError = error message from backend (e.g. “duplicate record”).
    // null if no error.
    const [backendError, setBackendError] = useState< string | null>(null);

    // submitting = true while saving to backend.

    // Used to disable buttons and show “Saving…”.
    // submitting - true while saving to backend 
    // used to disable buttons and show "saving...."
    const [submitting, setSubmitting] = useState(false);
    
    // delteTarget - which row we want to delete 
    // if null,delete dialog is closed 
    // if set to a row, delete dialog is open 

    const [deleteTarget, setDeleteTarget] = useState<InternshipDemand | null>(null);

    // deleting is true while delete request in progress 
    const [deleting, setDeleting] = useState(false);

    //open create dialog 

    //Define function openCreate.
    //setEditing(null) → we’re not editing existing, we’re creating new.
    const openCreate = () => {

        setEditing(null);
    

        // Reset the form to empty values.
        // year = filtered year if set, or current year.

        setForm({
            year: filters.year || defaultYear, 
            internshipType: "",
            schoolType: "",
            subject: "",
            requiredTeachers:"",
            studentCount:"",
            forecasted: true,
            }
        );

        //Clear validation and backend errors.
        setFormErrors({});
        setBackendError(null);
        //Open the create/edit dialog.
        setDialogOpen(true);

    };

    // Open edit dialog 
    // 
    const openEdit = (row: InternshipDemand) => {

        //we are editing this specific row 
        setEditing(row);

        // Fill the form with current values from the row.
        setForm({
            year: row.year,
            internshipType: row.internshipType,
            schoolType: row.schoolType,
            subject: row.subject,
            requiredTeachers: row.requiredTeachers,
            studentCount: row.studentCount,
            forecasted: row.forecasted,
        }

        );
        // Clear errors.
        // Open dialog.
        setFormErrors({});
        setBackendError(null);
        setDialogOpen(true);

    }

    // Helper to update form fields

    // Field: which field to change (e.g. "year").
    // Value: new value

    const updateFormField = (field: keyof DemandFormState, value: any) => 
    {

        //Take previous form object prev.
        //Copy all fields ...prev.
        //Replace one field: [field]: value.
        setForm((prev) => ({ ...prev, [field]: value }));  

    }

    // Duplicate detection 
    // -------- Duplicate detection (client-side) --------
    const duplicateExists = useMemo(() => {
    if (!form.year || !form.internshipType || !form.schoolType || !form.subject)
        return false;

    return data.some(
        (e) =>
        e.id !== editing?.id &&
        e.year === form.year &&
        e.internshipType === form.internshipType &&
        e.schoolType === form.schoolType &&
        e.subject.trim().toLowerCase() === form.subject.trim().toLowerCase()
    );
    }, [data, form, editing]);

    // -------- Validation --------
    const validateForm = () => {
        const errs: Record<string, string> = {};

        if (!form.year) errs.year = "Year is required";
        if (!form.internshipType) errs.internshipType = "Internship type is required";
        if (!form.schoolType) errs.schoolType = "School type is required";
        if (!form.subject.trim()) errs.subject = "Subject is required";

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

    // -------- Save (create or update) --------
    const handleSubmit = async (e: React.FormEvent) => {e.preventDefault();
        if (!validateForm()) return;

        const payload = {
            year: Number(form.year),
            internshipType: form.internshipType,
            schoolType: form.schoolType,
            subject: form.subject.trim(),
            requiredTeachers: Number(form.requiredTeachers),
            studentCount: Number(form.studentCount),
            forecasted: form.forecasted,
        };

        try {
            setSubmitting(true);
            setBackendError(null);

            if (editing) {
                await updateInternshipDemand(editing.id, payload);
            } else {
                await createInternshipDemand(payload);
            }

        setDialogOpen(false);
        await loadData();
        } catch (err: any) {
            setBackendError(err?.message || "Failed to save internship demand");
        } finally {
            setSubmitting(false);
        }
    };

    // -------- Delete --------
    const handleConfirmDelete = async () => {
        if (!deleteTarget) return;
        try {
            setDeleting(true);
            await deleteInternshipDemand(deleteTarget.id);
            setDeleteTarget(null);
            await loadData();
            } catch (err: any) {
            setError(err?.message || "Failed to delete internship demand");
            } finally {
        setDeleting(false);
            }
    };

     // -------- DataTable actions config --------
    const tableActions: DataTableActions<InternshipDemand> | undefined = isAdmin 
    ? {
        onEdit: (row) => openEdit(row),
        onDelete: (row) => setDeleteTarget(row),
    }
    : undefined;
    
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
            <CardHeader>
                <CardTitle className="text-base">Filters</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-4 md:grid-cols-5">
                <div className="space-y-1">
                <Label htmlFor="filter-year">Year *</Label>
                <Input
                    id="filter-year"
                    type="number"
                    value={filters.year ?? ""}
                    onChange={(e) =>
                        setFilters((f) => ({
                            ...f,
                            year: e.target.value ? Number(e.target.value) : ("" as any),
                        }))
                    }
                    min={2000}
                />
                </div>
                <div className="space-y-1">
                    <Label htmlFor="filter-internshipType">Internship type</Label>
                    <Input
                        id="filter-internshipType"
                        value={filters.internshipType || ""}
                        onChange={(e) =>
                        setFilters((f) => ({ ...f, internshipType: e.target.value }))
                    }
                    placeholder="All"
                    />
                </div>
                <div className="space-y-1">
                <Label htmlFor="filter-schoolType">School type</Label>
                <Input
                    id="filter-schoolType"
                    value={filters.schoolType || ""}
                    onChange={(e) =>
                    setFilters((f) => ({ ...f, schoolType: e.target.value }))
                }
                placeholder="All"
                />
                </div>
                <div className="space-y-1">
                <Label htmlFor="filter-subject">Subject</Label>
                <Input
                    id="filter-subject"
                    value={filters.subject || ""}
                    onChange={(e) =>
                    setFilters((f) => ({ ...f, subject: e.target.value }))
                    }
                placeholder="All"
                />
                </div>
                <div className="flex items-end gap-2">
                    <Checkbox
                        id="filter-onlyForecasted"
                        checked={filters.onlyForecasted}
                        onCheckedChange={(checked) =>
                        setFilters((f) => ({
                        ...f,
                        onlyForecasted: Boolean(checked),
                    }))
                    }
                />
                <Label htmlFor="filter-onlyForecasted" className="text-xs">
                    Show only forecasted
                </Label>
                </div>
            </CardContent>
        </Card>

        {/* DataTable */}
        <Card>
            <CardHeader>
                <CardTitle className="text-base">Demand list</CardTitle>
            </CardHeader>
            <CardContent>
            <DataTable<InternshipDemand>
                data={data}
                columnConfig={internshipDemandColumns}
                loading={loading}
                error={error}
                emptyMessage="No internship demand found."
                enableSearch={true}
                searchKey="subject"
                enablePagination={true}
                actions={tableActions}
                actionsHeader="Actions"
                // We use our own dialog below, so disable internal dialog
                disableInternalDialog={true}
                enableRowSelection={false}
                enableRowClick={false}
            />
            </CardContent>
        </Card>

      {/* Create/Edit Dialog */}
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
            <DialogHeader>
                <DialogTitle>
                    {editing ? "Edit internship demand" : "Create internship demand"}
                </DialogTitle>
            </DialogHeader>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-1">
                        <Label htmlFor="form-year">Year *</Label>
                        <Input
                            id="form-year"
                            type="number"
                            value={form.year ?? ""}
                            onChange={(e) =>
                            updateFormField(
                            "year",
                            e.target.value ? Number(e.target.value) : ""
                            )
                            }
                        />
                        {formErrors.year && (
                            <p className="text-xs text-destructive">{formErrors.year}</p>
                        )}
                    </div>

                <div className="space-y-1">
                    <Label htmlFor="form-internshipType">Internship type *</Label>
                    <Input
                        id="form-internshipType"
                        value={form.internshipType}
                        onChange={(e) =>
                            updateFormField("internshipType", e.target.value)
                        }
                    />
                    {formErrors.internshipType && (
                        <p className="text-xs text-destructive">
                            {formErrors.internshipType}
                        </p>
                    )}
                </div>

                <div className="space-y-1">
                <Label htmlFor="form-schoolType">School type *</Label>
                <Input
                    id="form-schoolType"
                    value={form.schoolType}
                    onChange={(e) =>
                        updateFormField("schoolType", e.target.value)
                }
                />
                {formErrors.schoolType && (
                    <p className="text-xs text-destructive">
                    {formErrors.schoolType}
                    </p>
                )}
                </div>

                <div className="space-y-1">
                    <Label htmlFor="form-subject">Subject *</Label>
                <Input
                    id="form-subject"
                    value={form.subject}
                    onChange={(e) =>
                        updateFormField("subject", e.target.value)
                    }
                />
                {formErrors.subject && (
                    <p className="text-xs text-destructive">
                        {formErrors.subject}
                    </p>
                )}
                </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-1">
                    <Label htmlFor="form-requiredTeachers">
                        Required teachers *
                    </Label>
                <Input
                    id="form-requiredTeachers"
                    type="number"
                    min={0}
                    value={form.requiredTeachers}
                    onChange={(e) =>
                    updateFormField(
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

                <div className="space-y-1">
                    <Label htmlFor="form-studentCount">Student count *</Label>
                        <Input
                            id="form-studentCount"
                            type="number"
                            min={0}
                            value={form.studentCount}
                            onChange={(e) =>
                            updateFormField(
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

            <div className="flex items-center gap-2">
                <Checkbox
                    id="form-forecasted"
                    checked={form.forecasted}
                    onCheckedChange={(checked) =>
                    updateFormField("forecasted", Boolean(checked))
                    }
                />
                <Label htmlFor="form-forecasted" className="text-xs">
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
                    onClick={() => setDialogOpen(false)}
                    disabled={submitting}
                >
                Cancel
                </Button>
                <Button type="submit" disabled={submitting}>
                    {submitting ? "Saving…" : "Save"}
                </Button>
            </DialogFooter>
            </form>
        </DialogContent>
        </Dialog>

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
        <AlertDialogAction onClick={handleConfirmDelete} disabled={deleting}>
            {deleting ? "Deleting…" : "Delete"}
        </AlertDialogAction>
        </AlertDialogFooter>
        </AlertDialogContent>
    </AlertDialog>
    </div>
    );
};

export default InternshipDemandPerYearPage;
