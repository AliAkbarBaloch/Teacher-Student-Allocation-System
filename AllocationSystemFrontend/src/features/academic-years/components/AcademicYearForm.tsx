import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { AlertCircle } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { AcademicYear, CreateAcademicYearRequest, UpdateAcademicYearRequest } from "../types/academicYear.types";
import { AcademicYearFormFields } from "@/features/academic-years/components/AcademicYearFormFields";
import {
  type FormErrors,
  buildSubmitPayload,
  clearFieldError,
  getInitialFormData,
  updateFormDataField,
  validateAcademicYearForm,
} from "./AcademicYearForm.helpers";

/**
 * Props for the AcademicYearForm component.
 * Used to create or update an academic year.
 */
interface AcademicYearFormProps {
  academicYear?: AcademicYear | null;
  onSubmit: (data: CreateAcademicYearRequest | UpdateAcademicYearRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

/**
 * Displays a validation or submission error message.
 */
function AcademicYearFormErrorBanner(props: { errorText?: string | null }) {
  const { errorText } = props;
  if (!errorText) {
    return null;
  }

  return (
    <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
      <AlertCircle className="h-4 w-4" />
      <span>{errorText}</span>
    </div>
  );
}

function AcademicYearFormContent(props: {
  bannerText: string | null;
  formData: CreateAcademicYearRequest;
  errors: FormErrors;
  disabled: boolean;
  academicYear?: AcademicYear | null;
  onCancel: () => void;
  onChange: (field: keyof CreateAcademicYearRequest, value: string | number | boolean | null) => void;
  onSubmit: (e: React.FormEvent) => void;
  tAcademicYears: (key: string) => string;
  tCommon: (key: string) => string;
}) {
  const {
    bannerText,
    formData,
    errors,
    disabled,
    academicYear,
    onCancel,
    onChange,
    onSubmit,
    tAcademicYears,
    tCommon,
  } = props;

  return (
    <form onSubmit={onSubmit} className="space-y-4 py-4">
      <AcademicYearFormErrorBanner errorText={bannerText} />

      <AcademicYearFormFields
        formData={formData}
        errors={errors}
        disabled={disabled}
        onChange={onChange}
        tAcademicYears={tAcademicYears}
      />

      <CheckboxField
        id="isLocked"
        checked={!!formData.isLocked}
        onCheckedChange={(val) => onChange("isLocked", val)}
        label={tAcademicYears("form.fields.isLocked")}
        description={tAcademicYears("form.fields.isLockedDescription")}
        statusText={formData.isLocked ? tAcademicYears("table.locked") : tAcademicYears("table.unlocked")}
        disabled={disabled}
      />

      <div className="flex justify-end gap-2 pt-2">
        <CancelButton onClick={onCancel} disabled={disabled}>
          {tCommon("actions.cancel")}
        </CancelButton>

        <SubmitButton
          isLoading={disabled}
          isEdit={!!academicYear}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={disabled}
        />
      </div>
    </form>
  );
}

export function AcademicYearForm({
  academicYear,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: AcademicYearFormProps) {
  const { t: tAcademicYears } = useTranslation("academicYears");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreateAcademicYearRequest>(() => getInitialFormData(academicYear));
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormData(getInitialFormData(academicYear));
    setErrors({});
  }, [academicYear]);

  const disabled = isLoading || isSubmitting;

  function handleChange(field: keyof CreateAcademicYearRequest, value: string | number | boolean | null): void {
    setFormData(updateFormDataField(field, value));
    if (errors[field]) {
      setErrors(clearFieldError(field));
    }
  }

  async function handleSubmit(e: React.FormEvent): Promise<void> {
    e.preventDefault();

    const newErrors = validateAcademicYearForm(formData, tAcademicYears);
    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(buildSubmitPayload(formData));
    } finally {
      setIsSubmitting(false);
    }
  }

  const bannerText = externalError || Object.values(errors)[0] || null;

  return (
    <AcademicYearFormContent
      bannerText={bannerText}
      formData={formData}
      errors={errors}
      disabled={disabled}
      academicYear={academicYear}
      onCancel={onCancel}
      onChange={handleChange}
      onSubmit={handleSubmit}
      tAcademicYears={tAcademicYears}
      tCommon={tCommon}
    />
  );
}