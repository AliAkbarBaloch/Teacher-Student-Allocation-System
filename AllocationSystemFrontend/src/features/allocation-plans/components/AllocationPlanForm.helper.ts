// AllocationPlanForm.helpers.ts
import type {
    AllocationPlan,
    CreateAllocationPlanRequest,
    PlanStatus,
    UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

/**
 * Validation errors for allocation plan form fields.
 */
export type AllocationPlanFormErrors = Partial<Record<keyof CreateAllocationPlanRequest, string>>;

export const PLAN_STATUS_OPTIONS: { value: PlanStatus; label: string }[] = [
    { value: "DRAFT", label: "Draft" },
    { value: "IN_REVIEW", label: "In Review" },
    { value: "APPROVED", label: "Approved" },
    { value: "ARCHIVED", label: "Archived" },
];

export function getInitialAllocationPlanFormData(allocationPlan?: AllocationPlan | null): CreateAllocationPlanRequest {
    if (!allocationPlan) {
        return {
            yearId: 0,
            planName: "",
            planVersion: "",
            status: "DRAFT",
            isCurrent: false,
            notes: "",
        };
    }

    return {
        yearId: allocationPlan.yearId,
        planName: allocationPlan.planName || "",
        planVersion: allocationPlan.planVersion || "",
        status: allocationPlan.status,
        isCurrent: allocationPlan.isCurrent ?? false,
        notes: allocationPlan.notes ?? "",
    };
}

export function validateAllocationPlanForm(
    formData: CreateAllocationPlanRequest,
    t: (key: string) => string
): AllocationPlanFormErrors {
    const errors: AllocationPlanFormErrors = {};

    if (!formData.yearId || formData.yearId < 1) { 
        errors.yearId = t("form.errors.yearIdRequired");
    }
    if (!formData.planName.trim()) { 
        errors.planName = t("form.errors.planNameRequired");
    }
    if (!formData.planVersion.trim()) {
        errors.planVersion = t("form.errors.planVersionRequired");
    }
    if (!formData.status) {
        errors.status = t("form.errors.statusRequired");
    }
    if (formData.notes && formData.notes.length > 5000) {
        errors.notes = t("form.errors.notesMaxLength");
    }

    return errors;
}

export function buildAllocationPlanSubmitPayload(
    formData: CreateAllocationPlanRequest,
    allocationPlan?: AllocationPlan | null
): CreateAllocationPlanRequest | UpdateAllocationPlanRequest {
    if (allocationPlan) {
        return {
            planName: formData.planName.trim(),
            status: formData.status,
            isCurrent: formData.isCurrent,
            notes: formData.notes,
        };
    }

    return {
        yearId: formData.yearId,
        planName: formData.planName.trim(),
        planVersion: formData.planVersion.trim(),
        status: formData.status,
        isCurrent: formData.isCurrent,
        notes: formData.notes,
    };
}
