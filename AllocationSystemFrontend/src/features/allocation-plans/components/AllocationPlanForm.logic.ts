// AllocationPlanForm.logic.ts
import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";

import { AcademicYearService, type AcademicYear } from "@/features/academic-years";
import type {
    AllocationPlan,
    CreateAllocationPlanRequest,
    UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

import {
    type AllocationPlanFormErrors,
    buildAllocationPlanSubmitPayload,
    getInitialAllocationPlanFormData,
    validateAllocationPlanForm,
} from "./AllocationPlanForm.helper";

type SelectOption = { value: string; label: string };

function buildYearOption(year: AcademicYear): SelectOption {
    return { value: String(year.id), label: year.yearName };
}

function buildYearOptions(years: AcademicYear[], t: (key: string) => string): SelectOption[] {
    return [{ value: "__none__", label: t("form.placeholders.yearId") }, ...years.map(buildYearOption)];
}

function updateFormDataField(
    field: keyof CreateAllocationPlanRequest,
    value: string | number | boolean | null
) {
    return function updater(prev: CreateAllocationPlanRequest): CreateAllocationPlanRequest {
        return { ...prev, [field]: value };
    };
}

function clearFieldError(field: keyof CreateAllocationPlanRequest) {
    return function updater(prev: AllocationPlanFormErrors): AllocationPlanFormErrors {
        return { ...prev, [field]: undefined };
    };
}

/**
 * Loads academic years for the year dropdown.
 */
function useAcademicYears() {
    const [years, setYears] = useState<AcademicYear[]>([]);
    const [loadingYears, setLoadingYears] = useState(true);

    useEffect(function loadAcademicYearsOnce() {
        async function loadYears(): Promise<void> {
            setLoadingYears(true);
            try {
                const data = await AcademicYearService.getAll();
                setYears(data);
            } catch {
                setYears([]);
            } finally {
                setLoadingYears(false);
            }
        }

        loadYears();
    }, []);

    return { years, loadingYears };
}

/**
 * Initializes and keeps the form data in sync with the selected allocation plan.
 */
function useAllocationPlanFormState(allocationPlan?: AllocationPlan | null) {
    const [formData, setFormData] = useState<CreateAllocationPlanRequest>(function initFormData() {
        return getInitialAllocationPlanFormData(allocationPlan);
    });

    const [errors, setErrors] = useState<AllocationPlanFormErrors>({});

    useEffect(
        function syncFormDataWhenSelectionChanges() {
            setFormData(getInitialAllocationPlanFormData(allocationPlan));
            setErrors({});
        },
        [allocationPlan]
    );

    return { formData, setFormData, errors, setErrors };
}

/**
 * Builds derived UI values for select fields.
 */
function useAllocationPlanDerivedValues(
    formData: CreateAllocationPlanRequest,
    years: AcademicYear[],
    t: (key: string) => string
) {
    const yearValue = useMemo(
        function computeYearValue() {
            return formData.yearId > 0 ? String(formData.yearId) : "__none__";
        },
        [formData.yearId]
    );

    const yearOptions = useMemo(
        function computeYearOptions() {
            return buildYearOptions(years, t);
        },
        [years, t]
    );

    return { yearValue, yearOptions };
}

/**
 * Manages state, validation, and submission for AllocationPlanForm.
 */
export function useAllocationPlanFormLogic(params: {
    allocationPlan?: AllocationPlan | null;
    isLoading: boolean;
    onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
    t: (key: string) => string;
}) {
    const { allocationPlan, isLoading, onSubmit, t } = params;

    const { years, loadingYears } = useAcademicYears();
    const { formData, setFormData, errors, setErrors } = useAllocationPlanFormState(allocationPlan);
    const { yearValue, yearOptions } = useAllocationPlanDerivedValues(formData, years, t);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const disabled = isLoading || isSubmitting;

    function handleChange(field: keyof CreateAllocationPlanRequest, value: string | number | boolean | null): void {
        setFormData(updateFormDataField(field, value));

        if (errors[field]) {
            setErrors(clearFieldError(field));
        }
    }

    async function handleSubmit(e: FormEvent): Promise<void> {
        e.preventDefault();
        if (loadingYears) {
            return;
        }

        const newErrors = validateAllocationPlanForm(formData, t);
        setErrors(newErrors);
        if (Object.keys(newErrors).length > 0) {
            return;
        }

        setIsSubmitting(true);
        try {
            await onSubmit(buildAllocationPlanSubmitPayload(formData, allocationPlan));
        } finally {
            setIsSubmitting(false);
        }
    }

    return { formData, errors, disabled, loadingYears, yearValue, yearOptions, handleChange, handleSubmit };
}
