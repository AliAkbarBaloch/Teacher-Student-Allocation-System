import React, { useEffect, useState } from "react";
// translations
import { useTranslation } from "react-i18next";
// components
import { TextField } from "@/components/form/fields/TextField";
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";

// icons
import { AlertCircle } from "lucide-react";

// types
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";

interface PlanChangeLogFormProps {
  planChangeLog?: PlanChangeLog | null;
  onSubmit: (
    data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest
  ) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function PlanChangeLogForm({
  planChangeLog,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: PlanChangeLogFormProps) {
  const { t } = useTranslation("planChangeLogs");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreatePlanChangeLogRequest>(() => {
    if (planChangeLog) {
      return {
        planId: planChangeLog.planId,
        changeType: planChangeLog.changeType || "",
        entityType: planChangeLog.entityType || "",
        entityId: planChangeLog.entityId,
        oldValue: planChangeLog.oldValue ?? "",
        newValue: planChangeLog.newValue ?? "",
        reason: planChangeLog.reason ?? "",
      };
    }
    return {
      planId: 0,
      changeType: "",
      entityType: "",
      entityId: 0,
      oldValue: "",
      newValue: "",
      reason: "",
    };
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof CreatePlanChangeLogRequest, string>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (planChangeLog) {
      setFormData({
        planId: planChangeLog.planId,
        changeType: planChangeLog.changeType || "",
        entityType: planChangeLog.entityType || "",
        entityId: planChangeLog.entityId,
        oldValue: planChangeLog.oldValue ?? "",
        newValue: planChangeLog.newValue ?? "",
        reason: planChangeLog.reason ?? "",
      });
    } else {
      setFormData({
        planId: 0,
        changeType: "",
        entityType: "",
        entityId: 0,
        oldValue: "",
        newValue: "",
        reason: "",
      });
    }
    setErrors({});
  }, [planChangeLog]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreatePlanChangeLogRequest, string>> =
      {};

    if (!formData.planId || isNaN(formData.planId)) {
      newErrors.planId = t("form.errors.planIdRequired");
    }
    if (!formData.changeType.trim()) {
      newErrors.changeType = t("form.errors.changeTypeRequired");
    } else if (formData.changeType.length > 50) {
      newErrors.changeType = t("form.errors.changeTypeMaxLength");
    }
    if (!formData.entityType.trim()) {
      newErrors.entityType = t("form.errors.entityTypeRequired");
    } else if (formData.entityType.length > 100) {
      newErrors.entityType = t("form.errors.entityTypeMaxLength");
    }
    if (!formData.entityId || isNaN(formData.entityId)) {
      newErrors.entityId = t("form.errors.entityIdRequired");
    }
    if (formData.reason && formData.reason.length > 500) {
      newErrors.reason = t("form.errors.reasonMaxLength");
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

      if (planChangeLog) {
        const updateData: UpdatePlanChangeLogRequest = {
          changeType: formData.changeType.trim(),
          entityType: formData.entityType.trim(),
          entityId: formData.entityId,
          oldValue: toOptionalString(formData.oldValue ?? ""),
          newValue: toOptionalString(formData.newValue ?? ""),
          reason: toOptionalString(formData.reason ?? ""),
        };
        await onSubmit(updateData);
      } else {
        const createData: CreatePlanChangeLogRequest = {
          planId: formData.planId,
          changeType: formData.changeType.trim(),
          entityType: formData.entityType.trim(),
          entityId: formData.entityId,
          oldValue: toOptionalString(formData.oldValue ?? ""),
          newValue: toOptionalString(formData.newValue ?? ""),
          reason: toOptionalString(formData.reason ?? ""),
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    field: keyof CreatePlanChangeLogRequest,
    value: string | number | null
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));

    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 py-4">
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <AlertCircle className="h-4 w-4" />
          <span>
            {externalError || Object.values(errors).filter(Boolean).join(", ")}
          </span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <TextField
          id="planId"
          label={t("form.fields.planId")}
          value={formData.planId.toString()}
          onChange={(val: string) => handleChange("planId", Number(val))}
          placeholder={t("form.placeholders.planId")}
          disabled={isLoading || isSubmitting}
          error={errors.planId}
          required={true}
        />

        <TextField
          id="changeType"
          label={t("form.fields.changeType")}
          value={formData.changeType}
          onChange={(val: string) => handleChange("changeType", val)}
          placeholder={t("form.placeholders.changeType")}
          disabled={isLoading || isSubmitting}
          error={errors.changeType}
          required={true}
          maxLength={50}
        />

        <TextField
          id="entityType"
          label={t("form.fields.entityType")}
          value={formData.entityType}
          onChange={(val: string) => handleChange("entityType", val)}
          placeholder={t("form.placeholders.entityType")}
          disabled={isLoading || isSubmitting}
          error={errors.entityType}
          required={true}
          maxLength={100}
        />

        <TextField
          id="entityId"
          label={t("form.fields.entityId")}
          value={formData.entityId.toString()}
          onChange={(val: string) => handleChange("entityId", Number(val))}
          placeholder={t("form.placeholders.entityId")}
          disabled={isLoading || isSubmitting}
          error={errors.entityId}
          required={true}
        />

        <TextField
          id="oldValue"
          label={t("form.fields.oldValue")}
          value={formData.oldValue ?? ""}
          onChange={(val: string) => handleChange("oldValue", val)}
          placeholder={t("form.placeholders.oldValue")}
          disabled={isLoading || isSubmitting}
          error={errors.oldValue}
        />

        <TextField
          id="newValue"
          label={t("form.fields.newValue")}
          value={formData.newValue ?? ""}
          onChange={(val: string) => handleChange("newValue", val)}
          placeholder={t("form.placeholders.newValue")}
          disabled={isLoading || isSubmitting}
          error={errors.newValue}
        />

        <TextField
          id="reason"
          label={t("form.fields.reason")}
          value={formData.reason ?? ""}
          onChange={(val: string) => handleChange("reason", val)}
          placeholder={t("form.placeholders.reason")}
          disabled={isLoading || isSubmitting}
          error={errors.reason}
          maxLength={500}
        />
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <CancelButton
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </CancelButton>
        <SubmitButton
          isLoading={isSubmitting || isLoading}
          isEdit={!!planChangeLog}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}
