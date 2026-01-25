import type {
    AcademicYear,
    CreateAcademicYearRequest,
    UpdateAcademicYearRequest,
} from "../types/academicYear.types";

/**
 * Validation errors for the academic year form fields.
 * Keys match CreateAcademicYearRequest fields.
 */
export type FormErrors = Partial<Record<keyof CreateAcademicYearRequest, string>>;

export function getEmptyFormData(): CreateAcademicYearRequest {
    return {
        yearName: "",
        totalCreditHours: 0,
        elementarySchoolHours: 0,
        middleSchoolHours: 0,
        budgetAnnouncementDate: "",
        allocationDeadline: null,
        isLocked: false,
    };
}

export function getInitialFormData(academicYear?: AcademicYear | null): CreateAcademicYearRequest {
    if (!academicYear) {
        return getEmptyFormData();
    }

    return {
        yearName: academicYear.yearName || "",
        totalCreditHours: academicYear.totalCreditHours || 0,
        elementarySchoolHours: academicYear.elementarySchoolHours || 0,
        middleSchoolHours: academicYear.middleSchoolHours || 0,
        budgetAnnouncementDate: academicYear.budgetAnnouncementDate || "",
        allocationDeadline: academicYear.allocationDeadline ?? null,
        isLocked: academicYear.isLocked ?? false,
    };
}

export function validateAcademicYearForm(
    formData: CreateAcademicYearRequest,
    t: (key: string) => string
): FormErrors {
    const newErrors: FormErrors = {};

    if (!formData.yearName.trim()) {
        newErrors.yearName = t("form.errors.yearNameRequired");
    }
    if (formData.totalCreditHours < 0) {
        newErrors.totalCreditHours = t("form.errors.totalCreditHoursRequired");
    }
    if (formData.elementarySchoolHours < 0) {
        newErrors.elementarySchoolHours = t("form.errors.elementarySchoolHoursRequired");
    }
    if (formData.middleSchoolHours < 0) { 
        newErrors.middleSchoolHours = t("form.errors.middleSchoolHoursRequired");
    }
    if (!formData.budgetAnnouncementDate.trim()) { 
        newErrors.budgetAnnouncementDate = t("form.errors.budgetAnnouncementDateRequired");
    }

    return newErrors;
}

export function buildSubmitPayload(
    formData: CreateAcademicYearRequest
): CreateAcademicYearRequest | UpdateAcademicYearRequest {
    return {
        yearName: formData.yearName.trim(),
        totalCreditHours: formData.totalCreditHours,
        elementarySchoolHours: formData.elementarySchoolHours,
        middleSchoolHours: formData.middleSchoolHours,
        budgetAnnouncementDate: formData.budgetAnnouncementDate,
        allocationDeadline: formData.allocationDeadline,
        isLocked: formData.isLocked,
    };
}

export function updateFormDataField(
    field: keyof CreateAcademicYearRequest,
    value: string | number | boolean | null
) {
    return function updater(prev: CreateAcademicYearRequest): CreateAcademicYearRequest {
        return { ...prev, [field]: value };
    };
}

export function clearFieldError(field: keyof CreateAcademicYearRequest) {
    return function updater(prev: FormErrors): FormErrors {
        return { ...prev, [field]: undefined };
    };
}
