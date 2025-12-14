import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { DateTimeField } from "@/components/form/fields/DateTimeField";
import { NumberField } from "@/components/form/fields/NumberField";
import { TextField } from "@/components/form/fields/TextField";
import { AlertCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";

interface AcademicYearFormProps {
  academicYear?: AcademicYear | null;
  onSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function AcademicYearForm({
  academicYear,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: AcademicYearFormProps) {
  const { t } = useTranslation("academicYears");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState<CreateAcademicYearRequest>(() => {
    if (academicYear) {
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
    return {
      yearName: "",
      totalCreditHours: 0,
      elementarySchoolHours: 0,
      middleSchoolHours: 0,
      budgetAnnouncementDate: "",
      allocationDeadline: null,
      isLocked: false,
    };
  });

  const [errors, setErrors] = useState<Partial<Record<keyof CreateAcademicYearRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (academicYear) {
      setFormData({
        yearName: academicYear.yearName || "",
        totalCreditHours: academicYear.totalCreditHours || 0,
        elementarySchoolHours: academicYear.elementarySchoolHours || 0,
        middleSchoolHours: academicYear.middleSchoolHours || 0,
        budgetAnnouncementDate: academicYear.budgetAnnouncementDate || "",
        allocationDeadline: academicYear.allocationDeadline ?? null,
        isLocked: academicYear.isLocked ?? false,
      });
    } else {
      setFormData({
        yearName: "",
        totalCreditHours: 0,
        elementarySchoolHours: 0,
        middleSchoolHours: 0,
        budgetAnnouncementDate: "",
        allocationDeadline: null,
        isLocked: false,
      });
    }
    setErrors({});
  }, [academicYear]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateAcademicYearRequest, string>> = {};

    if (!formData.yearName.trim()) {
      newErrors.yearName = t("form.errors.yearNameRequired");
    }
    if (!formData.totalCreditHours || formData.totalCreditHours < 0) {
      newErrors.totalCreditHours = t("form.errors.totalCreditHoursRequired");
    }
    if (!formData.elementarySchoolHours || formData.elementarySchoolHours < 0) {
      newErrors.elementarySchoolHours = t("form.errors.elementarySchoolHoursRequired");
    }
    if (!formData.middleSchoolHours || formData.middleSchoolHours < 0) {
      newErrors.middleSchoolHours = t("form.errors.middleSchoolHoursRequired");
    }
    if (!formData.budgetAnnouncementDate.trim()) {
      newErrors.budgetAnnouncementDate = t("form.errors.budgetAnnouncementDateRequired");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      if (academicYear) {
        const updateData: UpdateAcademicYearRequest = {
          yearName: formData.yearName.trim(),
          totalCreditHours: formData.totalCreditHours,
          elementarySchoolHours: formData.elementarySchoolHours,
          middleSchoolHours: formData.middleSchoolHours,
          budgetAnnouncementDate: formData.budgetAnnouncementDate,
          allocationDeadline: formData.allocationDeadline,
          isLocked: formData.isLocked,
        };
        await onSubmit(updateData);
      } else {
        const createData: CreateAcademicYearRequest = {
          yearName: formData.yearName.trim(),
          totalCreditHours: formData.totalCreditHours,
          elementarySchoolHours: formData.elementarySchoolHours,
          middleSchoolHours: formData.middleSchoolHours,
          budgetAnnouncementDate: formData.budgetAnnouncementDate,
          allocationDeadline: formData.allocationDeadline,
          isLocked: formData.isLocked,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateAcademicYearRequest, value: string | number | boolean | null) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 py-4">
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <AlertCircle className="h-4 w-4" />
          <span>{externalError || Object.values(errors)[0]}</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2 col-span-1">
          <TextField
            id="yearName"
            label={t("form.fields.yearName")}
            value={formData.yearName}
            onChange={val => handleChange("yearName", val)}
            placeholder={t("form.placeholders.yearName")}
            required
            error={errors.yearName}
            disabled={isLoading || isSubmitting}
            maxLength={100}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <NumberField
            id="totalCreditHours"
            label={t("form.fields.totalCreditHours")}
            value={formData.totalCreditHours}
            onChange={val => handleChange("totalCreditHours", val)}
            placeholder={t("form.placeholders.totalCreditHours")}
            required
            error={errors.totalCreditHours}
            disabled={isLoading || isSubmitting}
            min={0}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <NumberField
            id="elementarySchoolHours"
            label={t("form.fields.elementarySchoolHours")}
            value={formData.elementarySchoolHours}
            onChange={val => handleChange("elementarySchoolHours", val)}
            placeholder={t("form.placeholders.elementarySchoolHours")}
            required
            error={errors.elementarySchoolHours}
            disabled={isLoading || isSubmitting}
            min={0}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <NumberField
            id="middleSchoolHours"
            label={t("form.fields.middleSchoolHours")}
            value={formData.middleSchoolHours}
            onChange={val => handleChange("middleSchoolHours", val)}
            placeholder={t("form.placeholders.middleSchoolHours")}
            required
            error={errors.middleSchoolHours}
            disabled={isLoading || isSubmitting}
            min={0}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <DateTimeField
            id="budgetAnnouncementDate"
            label={t("form.fields.budgetAnnouncementDate")}
            value={formData.budgetAnnouncementDate}
            onChange={val => handleChange("budgetAnnouncementDate", val)}
            placeholder={t("form.placeholders.budgetAnnouncementDate")}
            required
            error={errors.budgetAnnouncementDate}
            disabled={isLoading || isSubmitting}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <DateTimeField
            id="allocationDeadline"
            label={t("form.fields.allocationDeadline")}
            value={formData.allocationDeadline ?? ""}
            onChange={val => handleChange("allocationDeadline", val || null)}
            placeholder={t("form.placeholders.allocationDeadline")}
            disabled={isLoading || isSubmitting}
          />
        </div>
      </div>

      <CheckboxField
        id="isLocked"
        checked={!!formData.isLocked}
        onCheckedChange={val => handleChange("isLocked", val)}
        label={t("form.fields.isLocked")}
        description={t("form.fields.isLockedDescription")}
        statusText={formData.isLocked ? t("table.locked") : t("table.unlocked")}
        disabled={isLoading || isSubmitting}
      />

      <div className="flex justify-end gap-2 pt-2">
        <CancelButton
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </CancelButton>
        <SubmitButton
          isLoading={isSubmitting || isLoading}
          isEdit={!!academicYear}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}