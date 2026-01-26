import { useEffect, useMemo, useState } from "react";
import { AcademicYearService, type AcademicYear } from "@/features/academic-years";
import type { AllocationPlan, CreateAllocationPlanRequest, UpdateAllocationPlanRequest } from "../types/allocationPlan.types";
import {
    type AllocationPlanFormErrors,
    buildAllocationPlanSubmitPayload,
    getInitialAllocationPlanFormData,
    validateAllocationPlanForm,
} from "./AllocationPlanForm.helpers";

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

    const [years, setYears] = useState<AcademicYear[]>([]);
    const [loadingYears, setLoadingYears] = useState(true);

    const [formData, setFormData] = useState<CreateAllocationPlanRequest>(() =>
        getInitialAllocationPlanFormData(allocationPlan)
    );
    const [errors, setErrors] = useState<AllocationPlanFormErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        setFormData(getInitialAllocationPlanFormData(allocationPlan));
        setErrors({});
    }, [allocationPlan]);

    useEffect(() => {
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

    const yearValue = useMemo(() => (formData.yearId > 0 ? String(formData.yearId) : "__none__"), [formData.yearId]);

    const yearOptions = useMemo(
        () => [{ value: "__none__", label: t("form.placeholders.yearId") }, ...years.map((y) => ({ value: String(y.id), label: y.yearName }))],
        [years, t]
    );

    const disabled = isLoading || isSubmitting;

    function handleChange(field: keyof CreateAllocationPlanRequest, value: string | number | boolean | null): void {
        setFormData((prev) => ({ ...prev, [field]: value }));
        if (errors[field]) setErrors((prev) => ({ ...prev, [field]: undefined }));
    }

    async function handleSubmit(e: React.FormEvent): Promise<void> {
        e.preventDefault();
        if (loadingYears) return;

        const newErrors = validateAllocationPlanForm(formData, t);
        setErrors(newErrors);
        if (Object.keys(newErrors).length > 0) return;

        setIsSubmitting(true);
        try {
            await onSubmit(buildAllocationPlanSubmitPayload(formData, allocationPlan));
        } finally {
            setIsSubmitting(false);
        }
    }

    return { formData, errors, disabled, loadingYears, yearValue, yearOptions, handleChange, handleSubmit };
}
