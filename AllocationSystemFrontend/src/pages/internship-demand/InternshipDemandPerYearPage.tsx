//
import React from "react";
// useState - remember values 
// useEffect - runs code when something changes 
// useMemo - remember a calculated value 
import {useEffect, useMemo, useState} from "react";

//styled button 
import {Button} from "@/components/ui/button";
//Card - box, CardHeader, CardTitle, CardContent - parts of that box
import {Card, CardHeader, CardTitle, CardContent} from  "@/components/ui/card";
// text/number field 
import {Input} from "@components/ui/input";
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
    type DataTableAction, 
} from "@/components/data-table/DataTable";

import {
    // get list 
    fetchInternshipDemand, 
    // create new item 
    createInternshipDemand,
    //update existing 
    updateIntenshipDemand,
    //remove item 
    deleteIntershipDemand,
} from "@/features/internship-demand/api";

import {
    // what one row looks like 
    IntershipDemand,
    // what the filter look like 
    DemandFilter, 
    // what the form values look like 
    DemandState,
} from "@/feature/internship-demand/types"
import { School, TruckElectric } from "lucide-react";


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
        filed: "year", 
        // text shown at the top of the column 
        title: "Year",
        //text alignment in cell 
        align: "left",
        //if used in forms, this is a number field 
        filedType: "number",
        // this filed is required in the form 
        filedRequired: true,
    },
    {
        //column internshipType 
        filed: "internshipType",
        title: "Internship Type",
        fieldType: "text",
        fieldRequired: true,

    },
    {
        // column schoolType 
        filed: "schoolType",
        title: "School Type",
        fieldType: "text",
        fieldRequired: true, 
    },
    {
        //column subject 
        filed: "subject",
        title: "Subject",
        fieldType: "text",
        fieldRequired: true, 
    }
    {
        //column requredTeacher 
        field:"requredTeachers",
        title: "Required Teachers",
        align: "right",
        filedType: "number",
        fileldRequred: true,
    },
    {
        //column studentCount 
        field : "studentCount",
        title: "Student Count",
        align: "right",
        format: "number",
        filedType: "number",
        filedRequired: true,

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

    // true for now. to decide whether show buttons like Add/Edit/Delete 
    const isAdmin = useIsAdmin; 

    //filter state (searh control)

    // useState - creates a piece of memory, filters - current filters, setFilters - function to change them. 
    // Initial filter values year, other fileds are empty, onlyForecasted = false  
    const [filters, setFilters] = useState<DemandFilter>(
        {
            year: defaultYear, 
            intershipType:"",
            schoolType:"",
            subject: "",
            onlyForecasted: false,
        });

    // data loading state (what we got from backend)

    //data - array of InternshipDemand, setData - function to update that array, array starts as empty  

    const [data, setData] = useState<IntershipDemand[]>([]);


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
            //show loading state 
            setLoading(true);
            //clear any old error 
            setError(null);

            //calls backend with current filters 
            //await - wait for response, list - array returned from backend, setDate(list) - update data with new list 
            const list = await fetchInternshipDemand(filters);
            //update data with new list 
            setData(list);
        } catch (e: any) 
        {
            //if somethng goes wrongs set an error message 
            setError(e?.message ?? "Failed to load internship demand")
        }
        finally 
        {
            //loading is done whethere or not  there was an error 
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
        filters.intershipType,
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

    const [form, setForm] = useState<DemansFormState>({
        year: defaultYear,
        internshiptype: "",
        schoolType: "",
        subject: "",
        requredTeachers:"",
        studentCount:"",
        forecated: true,
    });




    



}
