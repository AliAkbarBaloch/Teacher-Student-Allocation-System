import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { AlertCircle } from "lucide-react";
import type {
  PlanChangeLog,
  CreatePlanChangeLogRequest,
  UpdatePlanChangeLogRequest,
} from "../types/planChangeLog.types";

interface PlanChangeLogFormProps {
  planChangeLog?: PlanChangeLog | null;
  onSubmit: (data: CreatePlanChangeLogRequest | UpdatePlanChangeLogRequest) => Promise<void>;
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

  const [errors, setErrors] = useState<Partial<Record<keyof CreatePlanChangeLogRequest, string>>>({});
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
    const newErrors: Partial<Record<keyof CreatePlanChangeLogRequest, string>> = {};

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

  const handleChange = (field: keyof CreatePlanChangeLogRequest, value: string | number | null) => {
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
            {externalError ||
              Object.values(errors)
                .filter(Boolean)
                .join(", ")}
          </span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2 col-span-1">
          <Label htmlFor="planId">{t("form.fields.planId")}</Label>
          <Input
            id="planId"
            type="number"
            value={formData.planId}
            onChange={(e) => handleChange("planId", Number(e.target.value))}
            placeholder={t("form.placeholders.planId")}
            disabled={isLoading || isSubmitting}
            className={errors.planId ? "border-destructive" : ""}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="changeType">{t("form.fields.changeType")}</Label>
          <Input
            id="changeType"
            value={formData.changeType}
            onChange={(e) => handleChange("changeType", e.target.value)}
            placeholder={t("form.placeholders.changeType")}
            disabled={isLoading || isSubmitting}
            className={errors.changeType ? "border-destructive" : ""}
            maxLength={50}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="entityType">{t("form.fields.entityType")}</Label>
          <Input
            id="entityType"
            value={formData.entityType}
            onChange={(e) => handleChange("entityType", e.target.value)}
            placeholder={t("form.placeholders.entityType")}
            disabled={isLoading || isSubmitting}
            className={errors.entityType ? "border-destructive" : ""}
            maxLength={100}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="entityId">{t("form.fields.entityId")}</Label>
          <Input
            id="entityId"
            type="number"
            value={formData.entityId}
            onChange={(e) => handleChange("entityId", Number(e.target.value))}
            placeholder={t("form.placeholders.entityId")}
            disabled={isLoading || isSubmitting}
            className={errors.entityId ? "border-destructive" : ""}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="oldValue">{t("form.fields.oldValue")}</Label>
          <Input
            id="oldValue"
            value={formData.oldValue ?? ""}
            onChange={(e) => handleChange("oldValue", e.target.value)}
            placeholder={t("form.placeholders.oldValue")}
            disabled={isLoading || isSubmitting}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="newValue">{t("form.fields.newValue")}</Label>
          <Input
            id="newValue"
            value={formData.newValue ?? ""}
            onChange={(e) => handleChange("newValue", e.target.value)}
            placeholder={t("form.placeholders.newValue")}
            disabled={isLoading || isSubmitting}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="reason">{t("form.fields.reason")}</Label>
          <Input
            id="reason"
            value={formData.reason ?? ""}
            onChange={(e) => handleChange("reason", e.target.value)}
            placeholder={t("form.placeholders.reason")}
            disabled={isLoading || isSubmitting}
            maxLength={500}
          />
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <Button
          type="button"
          variant="secondary"
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </Button>
        <Button
          type="submit"
          variant="default"
          disabled={isLoading || isSubmitting}
        >
          {planChangeLog ? tCommon("actions.save") : tCommon("actions.create")}
        </Button>
      </div>
    </form>
  );
}