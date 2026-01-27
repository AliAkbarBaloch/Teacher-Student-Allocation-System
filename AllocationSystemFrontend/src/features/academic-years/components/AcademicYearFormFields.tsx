import React from "react";
import { TextField } from "@/components/form/fields/TextField";
import { NumberField } from "@/components/form/fields/NumberField";
import { DateTimeField } from "@/components/form/fields/DateTimeField";
import type { CreateAcademicYearRequest } from "../types/academicYear.types";
import type { FormErrors } from "./AcademicYearForm.helpers";


/**
 * Wrapper for a single form field in the grid layout.
 */
function FieldCell(props: { children: React.ReactNode }) {
    return <div className="space-y-2 col-span-1">{props.children}</div>;
}

function NumberInputField(props: {
    id: string;
    label: string;
    value: number;
    error?: string;
    disabled: boolean;
    min?: number;
    onChange: (value: number) => void;
    placeholder: string;
}) {
    const { id, label, value, error, disabled, min, onChange, placeholder } = props;

    return (
        <FieldCell>
            <NumberField
                id={id}
                label={label}
                value={value}
                onChange={(val) => onChange(typeof val === "string" ? (val === "" ? 0 : Number(val)) : val)}
                placeholder={placeholder}
                required
                error={error}
                disabled={disabled}
                min={min}
            />
        </FieldCell>
    );
}

/**
 * Props for the AcademicYearFormFields component.
 */
export interface AcademicYearFormFieldsProps {
    formData: CreateAcademicYearRequest;
    errors: FormErrors;
    disabled: boolean;
    onChange: (
        field: keyof CreateAcademicYearRequest,
        value: string | number | boolean | null
    ) => void;
    tAcademicYears: (key: string) => string;
}



function YearNameField(props: AcademicYearFormFieldsProps) {
    const { formData, errors, disabled, onChange, tAcademicYears } = props;

    return (
        <FieldCell>
            <TextField
                id="yearName"
                label={tAcademicYears("form.fields.yearName")}
                value={formData.yearName}
                onChange={(val) => onChange("yearName", val)}
                placeholder={tAcademicYears("form.placeholders.yearName")}
                required
                error={errors.yearName}
                disabled={disabled}
                maxLength={100}
            />
        </FieldCell>
    );
}

function TotalCreditHoursField(props: AcademicYearFormFieldsProps) {
    const { formData, errors, disabled, onChange, tAcademicYears } = props;

    return (
        <NumberInputField
            id="totalCreditHours"
            label={tAcademicYears("form.fields.totalCreditHours")}
            value={formData.totalCreditHours}
            error={errors.totalCreditHours}
            disabled={disabled}
            min={0}
            placeholder={tAcademicYears("form.placeholders.totalCreditHours")}
            onChange={(val) => onChange("totalCreditHours", val)}
        />
    );
}


function ElementarySchoolHoursField(props: AcademicYearFormFieldsProps) {
    const { formData, errors, disabled, onChange, tAcademicYears } = props;

    return (
        <NumberInputField
            id="elementarySchoolHours"
            label={tAcademicYears("form.fields.elementarySchoolHours")}
            value={formData.elementarySchoolHours}
            error={errors.elementarySchoolHours}
            disabled={disabled}
            min={0}
            placeholder={tAcademicYears("form.placeholders.elementarySchoolHours")}
            onChange={(val) => onChange("elementarySchoolHours", val)}
        />
    );
}

function CreditHoursFields(props: AcademicYearFormFieldsProps) {
    return (
        <>
            <TotalCreditHoursField {...props} />
            <ElementarySchoolHoursField {...props} />
        </>
    );
}

function MiddleSchoolHoursField(props: AcademicYearFormFieldsProps) {
    const { formData, errors, disabled, onChange, tAcademicYears } = props;

    return (
        <NumberInputField
            id="middleSchoolHours"
            label={tAcademicYears("form.fields.middleSchoolHours")}
            value={formData.middleSchoolHours}
            error={errors.middleSchoolHours}
            disabled={disabled}
            min={0}
            placeholder={tAcademicYears("form.placeholders.middleSchoolHours")}
            onChange={(val) => onChange("middleSchoolHours", val)}
        />
    );
}


function DatesFields(props: AcademicYearFormFieldsProps) {
    const { formData, errors, disabled, onChange, tAcademicYears } = props;

    return (
        <>
            <FieldCell>
                <DateTimeField
                    id="budgetAnnouncementDate"
                    label={tAcademicYears("form.fields.budgetAnnouncementDate")}
                    value={formData.budgetAnnouncementDate}
                    onChange={(val) => onChange("budgetAnnouncementDate", val)}
                    placeholder={tAcademicYears("form.placeholders.budgetAnnouncementDate")}
                    required
                    error={errors.budgetAnnouncementDate}
                    disabled={disabled}
                />
            </FieldCell>

            <FieldCell>
                <DateTimeField
                    id="allocationDeadline"
                    label={tAcademicYears("form.fields.allocationDeadline")}
                    value={formData.allocationDeadline ?? ""}
                    onChange={(val) => onChange("allocationDeadline", val || null)}
                    placeholder={tAcademicYears("form.placeholders.allocationDeadline")}
                    disabled={disabled}
                />
            </FieldCell>
        </>
    );
}

/**
 * Renders all form fields for creating/updating an academic year.
 */
export function AcademicYearFormFields(props: AcademicYearFormFieldsProps) {
    return (
        <div className="grid gap-4 md:grid-cols-2">
            <YearNameField {...props} />
            <CreditHoursFields {...props} />
            <MiddleSchoolHoursField {...props} />
            <DatesFields {...props} />
        </div>
    );
}
