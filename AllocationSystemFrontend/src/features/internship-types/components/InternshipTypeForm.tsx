import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";

import type {
  InternshipType,
  CreateInternshipTypeRequest,
  UpdateInternshipTypeRequest,
} from "../types/internshipType.types";
import {
  TIMING_OPTIONS,
  PERIOD_TYPE_OPTIONS,
  SEMESTER_OPTIONS,
} from "../types/internshipType.types";
import { TextField } from "@/components/form/fields/TextField";
import { SelectField } from "@/components/form/fields/SelectField";
import { NumberField } from "@/components/form/fields/NumberField";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { Loader2 } from "lucide-react";

interface InternshipTypeFormProps {
  internshipType?: InternshipType | null;
  onSubmit: (
    data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest
  ) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export function InternshipTypeForm({
  internshipType,
  onSubmit,
  onCancel,
  isLoading = false,
}: InternshipTypeFormProps) {
  const { t } = useTranslation("internshipTypes");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState(() => {
    if (internshipType) {
      return {
        internshipCode: internshipType.internshipCode || "",
        fullName: internshipType.fullName || "",
        timing: internshipType.timing ?? "",
        periodType: internshipType.periodType ?? "",
        semester: internshipType.semester ?? "",
        isSubjectSpecific: internshipType.isSubjectSpecific ?? false,
        priorityOrder: internshipType.priorityOrder ?? null,
      };
    }
    return {
      internshipCode: "",
      fullName: "",
      timing: "",
      periodType: "",
      semester: "",
      isSubjectSpecific: false,
      priorityOrder: null as number | null,
    };
  });
  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateInternshipTypeRequest, string>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (internshipType) {
      setFormData({
        internshipCode: internshipType.internshipCode || "",
        fullName: internshipType.fullName || "",
        timing: internshipType.timing ?? "",
        periodType: internshipType.periodType ?? "",
        semester: internshipType.semester ?? "",
        isSubjectSpecific: internshipType.isSubjectSpecific ?? false,
        priorityOrder: internshipType.priorityOrder ?? null,
      });
    } else {
      setFormData({
        internshipCode: "",
        fullName: "",
        timing: "",
        periodType: "",
        semester: "",
        isSubjectSpecific: false,
        priorityOrder: null,
      });
    }
    setErrors({});
  }, [internshipType]);

  const validate = (): boolean => {
    const newErrors: Partial<
      Record<keyof CreateInternshipTypeRequest, string>
    > = {};

    if (!formData.internshipCode.trim()) {
      newErrors.internshipCode = t("form.errors.codeRequired");
    } else if (formData.internshipCode.length > 50) {
      newErrors.internshipCode = t("form.errors.codeMaxLength");
    }

    if (!formData.fullName.trim()) {
      newErrors.fullName = t("form.errors.fullNameRequired");
    } else if (formData.fullName.length > 255) {
      newErrors.fullName = t("form.errors.fullNameMaxLength");
    }

    if (!formData.timing || !formData.timing.trim()) {
      newErrors.timing = t("form.errors.timingRequired");
    } else if (formData.timing.length > 100) {
      newErrors.timing = t("form.errors.timingMaxLength");
    }

    if (!formData.periodType || !formData.periodType.trim()) {
      newErrors.periodType = t("form.errors.periodTypeRequired");
    } else if (formData.periodType.length > 50) {
      newErrors.periodType = t("form.errors.periodTypeMaxLength");
    }

    if (!formData.semester || !formData.semester.trim()) {
      newErrors.semester = t("form.errors.semesterRequired");
    } else if (formData.semester.length > 50) {
      newErrors.semester = t("form.errors.semesterMaxLength");
    }

    if (
      formData.priorityOrder !== null &&
      formData.priorityOrder !== undefined &&
      formData.priorityOrder < 0
    ) {
      newErrors.priorityOrder = t("form.errors.priorityOrderInvalid");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      // Helper to convert empty strings to null for optional fields
      const toOptionalString = (value: string): string | null => {
        const trimmed = value.trim();
        return trimmed === "" ? null : trimmed;
      };

      if (internshipType) {
        // For updates, send all fields - backend UpdateDto accepts all optional fields
        // Backend service checks != null before updating each field
        const updateData: UpdateInternshipTypeRequest = {
          internshipCode: formData.internshipCode.trim(),
          fullName: formData.fullName.trim(),
          timing: toOptionalString(formData.timing),
          periodType: toOptionalString(formData.periodType),
          semester: toOptionalString(formData.semester),
          isSubjectSpecific: formData.isSubjectSpecific,
          priorityOrder: formData.priorityOrder ?? null,
        };

        await onSubmit(updateData);
      } else {
        // For create, send all required fields
        const createData: CreateInternshipTypeRequest = {
          internshipCode: formData.internshipCode.trim(),
          fullName: formData.fullName.trim(),
          timing: toOptionalString(formData.timing),
          periodType: toOptionalString(formData.periodType),
          semester: toOptionalString(formData.semester),
          isSubjectSpecific: formData.isSubjectSpecific,
          priorityOrder: formData.priorityOrder ?? null,
        };
        await onSubmit(createData);
      }
    } catch {
      // Error handling is done by parent component
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    field: keyof CreateInternshipTypeRequest,
    value: string | boolean | number | null
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 py-4">

      <div className="grid gap-4 md:grid-cols-2 justify-center">
        <TextField
          id="fullName"
          label={t("form.fields.fullName")}
          value={formData.fullName}
          onChange={(val: string) => handleChange("fullName", val)}
          placeholder={t("form.placeholders.fullName")}
          disabled={isLoading || isSubmitting}
          maxLength={255}
          error={errors.fullName}
        />
        <TextField
          id="internshipCode"
          label={t("form.fields.code")}
          value={formData.internshipCode}
          onChange={(val: string) => handleChange("internshipCode", val)}
          placeholder={t("form.placeholders.code")}
          disabled={isLoading || isSubmitting}
          maxLength={20}
          error={errors.internshipCode}
        />

        <SelectField
          id="timing"
          label={t("form.fields.timing")}
          value={formData.timing}
          onChange={(val: string) => handleChange("timing", val)}
          placeholder={t("form.placeholders.timing")}
          disabled={isLoading || isSubmitting}
          options={TIMING_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
          error={errors.timing}
          required={true}
        />

        <SelectField
          id="periodType"
          label={t("form.fields.periodType")}
          value={formData.periodType}
          onChange={(val: string) => handleChange("periodType", val)}
          placeholder={t("form.placeholders.periodType")}
          disabled={isLoading || isSubmitting}
          options={PERIOD_TYPE_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
          error={errors.periodType}
          required={true}
        />

        <SelectField
          id="semester"
          label={t("form.fields.semester")}
          value={formData.semester}
          onChange={(val: string) => handleChange("semester", val)}
          placeholder={t("form.placeholders.semester")}
          disabled={isLoading || isSubmitting}
          options={SEMESTER_OPTIONS.map((option) => ({
            value: option.value,
            label: option.label,
          }))}
          error={errors.semester}
          required={true}
        />

        <NumberField
          id="priorityOrder"
          label={t("form.fields.priorityOrder")}
          value={formData.priorityOrder ?? 1}
          onChange={(val: number | string) => handleChange("priorityOrder", typeof val === "string" ? (val === "" ? null : Number(val)) : val)}
          placeholder={t("form.placeholders.priorityOrder")}
          disabled={isLoading || isSubmitting}
          error={errors.priorityOrder}
          min={0}
        />
      </div>

      <CheckboxField
        id="isSubjectSpecific"
        checked={formData.isSubjectSpecific}
        onCheckedChange={(checked) =>
          handleChange("isSubjectSpecific", checked === true)
        }
        label={t("form.fields.isSubjectSpecific")}
        description={t("form.fields.isSubjectSpecificDescription")}
        disabled={isLoading || isSubmitting}
      />

      <div className="flex justify-end gap-2 pt-4">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </Button>
        <Button type="submit" disabled={isLoading || isSubmitting}>
          {isSubmitting || isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              {tCommon("actions.saving")}
            </>
          ) : internshipType ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}
