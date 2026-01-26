import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { AlertCircle } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type {
  AcademicYear,
  CreateAcademicYearRequest,
  UpdateAcademicYearRequest,
} from "../types/academicYear.types";
import {
  buildAcademicYearSubmitPayload,
  getInitialAcademicYearFormData,
  validateAcademicYearForm,
} from "./AcademicYearForm.helpers";

type FormErrors = Partial<Record<keyof CreateAcademicYearRequest, string>>;

/**
 * Displays validation or submission errors.
 */
function AcademicYearFormErrorBanner(props: { text?: string | null }) {
  if (!props.text) {
    return null;
  }

  return (
    <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
      <AlertCircle className="h-4 w-4" />
      <span>{props.text}</span>
    </div>
  );
}

interface AcademicYearFormFieldsProps {
  formData: CreateAcademicYearRequest;
  errors: FormErrors;
  disabled: boolean;
  t: (key: string) => string;
  onChange: (
    field: keyof CreateAcademicYearRequest,
    value: string | number | boolean | null
  ) => void;
}

function AcademicYearFormFieldsGrid(props: AcademicYearFormFieldsProps) {
  const { formData, errors, disabled, t, onChange } = props;

  return (
    <div className="grid gap-4 md:grid-cols-2">
      {/* name + total hours */}
      <input />

      {/* school hours */}
      <input />

      {/* dates */}
      <input />
    </div>
  );
}

async function submitAcademicYearForm(params: {
  e: React.FormEvent;
  formData: CreateAcademicYearRequest;
  t: (key: string) => string;
  onSubmit: (
    data: CreateAcademicYearRequest | UpdateAcademicYearRequest
  ) => Promise<void>;
  setErrors: React.Dispatch<React.SetStateAction<FormErrors>>;
  setIsSubmitting: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  const { e, formData, t, onSubmit, setErrors, setIsSubmitting } = params;

  e.preventDefault();

  const newErrors = validateAcademicYearForm(formData, t);
  setErrors(newErrors);

  if (Object.keys(newErrors).length > 0) {
    return;
  }

  setIsSubmitting(true);
  try {
    await onSubmit(buildAcademicYearSubmitPayload(formData));
  } finally {
    setIsSubmitting(false);
  }
}

function useAcademicYearFormLogic(params: {
  academicYear?: AcademicYear | null;
  isLoading: boolean;
  onSubmit: (
    data: CreateAcademicYearRequest | UpdateAcademicYearRequest
  ) => Promise<void>;
  t: (key: string) => string;
}) {
  const { academicYear, isLoading, onSubmit, t } = params;

  const [formData, setFormData] = useState<CreateAcademicYearRequest>(() =>
    getInitialAcademicYearFormData(academicYear)
  );
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setFormData(getInitialAcademicYearFormData(academicYear));
    setErrors({});
  }, [academicYear]);

  const disabled = isLoading || isSubmitting;

  function handleChange(
    field: keyof CreateAcademicYearRequest,
    value: string | number | boolean | null
  ) {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  }

  function handleSubmit(e: React.FormEvent) {
    return submitAcademicYearForm({
      e,
      formData,
      t,
      onSubmit,
      setErrors,
      setIsSubmitting,
    });
  }

  return { formData, errors, disabled, handleChange, handleSubmit };
}

function AcademicYearFormActions(props: {
  disabled: boolean;
  isEdit: boolean;
  onCancel: () => void;
  tCommon: (key: string) => string;
}) {
  const { disabled, isEdit, onCancel, tCommon } = props;

  return (
    <div className="flex justify-end gap-2 pt-2">
      <CancelButton onClick={onCancel} disabled={disabled}>
        {tCommon("actions.cancel")}
      </CancelButton>
      <SubmitButton
        isLoading={disabled}
        isEdit={isEdit}
        createText={tCommon("actions.create")}
        updateText={tCommon("actions.update")}
        savingText={tCommon("actions.saving")}
        disabled={disabled}
      />
    </div>
  );
}

function AcademicYearLockCheckbox(props: {
  isLocked: boolean;
  disabled: boolean;
  onChange: (value: boolean | null) => void;
  t: (key: string) => string;
}) {
  const { isLocked, disabled, onChange, t } = props;

  return (
    <CheckboxField
      id="isLocked"
      checked={isLocked}
      onCheckedChange={onChange}
      label={t("form.fields.isLocked")}
      description={t("form.fields.isLockedDescription")}
      statusText={isLocked ? t("table.locked") : t("table.unlocked")}
      disabled={disabled}
    />
  );
}

export function AcademicYearForm(props: {
  academicYear?: AcademicYear | null;
  onSubmit: (
    data: CreateAcademicYearRequest | UpdateAcademicYearRequest
  ) => Promise<voi
