import type { AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest } from "../types/academicYear.types";

/**
 * Validation errors for the academic year form fields.
 * Keys match CreateAcademicYearRequest fields.
 */
export type FormErrors = Partial<Record<keyof CreateAcademicYearRequest, string>>;

export function getEmptyAcademicYearFormData(): CreateAcademicYearRequest {
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

export function getInitialAcademicYearFormData(academicYear?: AcademicYear | null): CreateAcademicYearRequest {
    if (!academicYear) {
        return getEmptyAcademicYearFormData();
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

export function validateAcademicYearForm(formData: CreateAcademicYearRequest, t: (key: string) => string): FormErrors {
    const errors: FormErrors = {};

    if (!formData.yearName.trim()) {
        errors.yearName = t("form.errors.yearNameRequired");
    }
    if (formData.totalCreditHours < 0) {
        errors.totalCreditHours = t("form.errors.totalCreditHoursRequired");
    }
    if (formData.elementarySchoolHours < 0) {
        errors.elementarySchoolHours = t("form.errors.elementarySchoolHoursRequired");
    }
    if (formData.middleSchoolHours < 0) {
        errors.middleSchoolHours = t("form.errors.middleSchoolHoursRequired");
    }
    if (!formData.budgetAnnouncementDate.trim()) {
        errors.budgetAnnouncementDate = t("form.errors.budgetAnnouncementDateRequired");
    }
    
    return errors;
}

export function buildAcademicYearSubmitPayload(
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
