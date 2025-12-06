import { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { AlertCircle, Loader2 } from "lucide-react";
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

interface InternshipTypeFormProps {
  internshipType?: InternshipType | null;
  onSubmit: (data: CreateInternshipTypeRequest | UpdateInternshipTypeRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function InternshipTypeForm({
  internshipType,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
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
  const [errors, setErrors] = useState<Partial<Record<keyof CreateInternshipTypeRequest, string>>>({});
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

  const timingValue = useMemo((): string => {
    const trimmed = formData.timing?.trim();
    return trimmed && trimmed.length > 0 ? trimmed : "__none__";
  }, [formData.timing]);

  const periodTypeValue = useMemo((): string => {
    const trimmed = formData.periodType?.trim();
    return trimmed && trimmed.length > 0 ? trimmed : "__none__";
  }, [formData.periodType]);

  const semesterValue = useMemo((): string => {
    if (formData.semester === null || formData.semester === undefined || formData.semester === "") {
      return "__none__";
    }
    return String(formData.semester);
  }, [formData.semester]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateInternshipTypeRequest, string>> = {};

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

    if (formData.timing && formData.timing.length > 100) {
      newErrors.timing = t("form.errors.timingMaxLength");
    }

    if (formData.periodType && formData.periodType.length > 50) {
      newErrors.periodType = t("form.errors.periodTypeMaxLength");
    }

    if (formData.semester && formData.semester.length > 50) {
      newErrors.semester = t("form.errors.semesterMaxLength");
    }

    if (formData.priorityOrder !== null && formData.priorityOrder !== undefined && formData.priorityOrder < 0) {
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

  const handleChange = (field: keyof CreateInternshipTypeRequest, value: string | boolean | number | null) => {
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
          <span>{externalError || Object.values(errors)[0]}</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 justify-center">
        <div className="space-y-2 col-span-1">
          <label htmlFor="fullName" className="text-sm font-medium">
            {t("form.fields.fullName")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="fullName"
            value={formData.fullName}
            onChange={(e) => handleChange("fullName", e.target.value)}
            placeholder={t("form.placeholders.fullName")}
            disabled={isLoading || isSubmitting}
            className={errors.fullName ? "border-destructive" : ""}
            maxLength={255}
          />
          {errors.fullName && (
            <p className="text-sm text-destructive">{errors.fullName}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="internshipCode" className="text-sm font-medium">
            {t("form.fields.code")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="internshipCode"
            value={formData.internshipCode}
            onChange={(e) => handleChange("internshipCode", e.target.value)}
            placeholder={t("form.placeholders.code")}
            disabled={isLoading || isSubmitting}
            className={errors.internshipCode ? "border-destructive" : ""}
            maxLength={50}
          />
          {errors.internshipCode && (
            <p className="text-sm text-destructive">{errors.internshipCode}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="timing" className="text-sm font-medium">
            {t("form.fields.timing")}
          </label>
          <Select
            value={timingValue}
            onValueChange={(value) =>
              handleChange("timing", value === "__none__" ? "" : value || "")
            }
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger
              className={`w-full ${errors.timing ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.timing")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.none")}
              </SelectItem>
              {TIMING_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.timing && (
            <p className="text-sm text-destructive">{errors.timing}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="periodType" className="text-sm font-medium">
            {t("form.fields.periodType")}
          </label>
          <Select
            value={periodTypeValue}
            onValueChange={(value) =>
              handleChange("periodType", value === "__none__" ? "" : value || "")
            }
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger
              className={`w-full ${errors.periodType ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.periodType")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.none")}
              </SelectItem>
              {PERIOD_TYPE_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.periodType && (
            <p className="text-sm text-destructive">{errors.periodType}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="semester" className="text-sm font-medium">
            {t("form.fields.semester")}
          </label>
          <Select
            value={semesterValue}
            onValueChange={(value) =>
              handleChange("semester", value === "__none__" ? "" : value || "")
            }
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger
              className={`w-full ${errors.semester ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.semester")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.none")}
              </SelectItem>
              {SEMESTER_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.semester && (
            <p className="text-sm text-destructive">{errors.semester}</p>
          )}
        </div>

          <div className="space-y-2 col-span-1">
            <label htmlFor="priorityOrder" className="text-sm font-medium">
              {t("form.fields.priorityOrder")}
            </label>
            <Input
              id="priorityOrder"
              type="number"
              min={0}
              step={1}
              value={formData.priorityOrder ?? ""}
              onChange={(e) =>
                handleChange(
                  "priorityOrder",
                  e.target.value ? parseInt(e.target.value, 10) : null
                )
              }
              placeholder={t("form.placeholders.priorityOrder")}
              disabled={isLoading || isSubmitting}
              className={errors.priorityOrder ? "border-destructive" : ""}
            />
            {errors.priorityOrder && (
              <p className="text-sm text-destructive">{errors.priorityOrder}</p>
            )}
          </div>

      </div>

      <Label
        htmlFor="isSubjectSpecific"
        className="hover:bg-accent/50 flex items-start gap-3 rounded-lg border p-4 cursor-pointer has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10 transition-colors"
      >
        <Checkbox
          id="isSubjectSpecific"
          checked={formData.isSubjectSpecific}
          onCheckedChange={(checked) =>
            handleChange("isSubjectSpecific", checked === true)
          }
          disabled={isLoading || isSubmitting}
          className="h-5 w-5 mt-0.5 data-[state=checked]:border-primary data-[state=checked]:bg-primary"
        />
        <div className="grid gap-1.5 flex-1">
          <p className="text-sm font-medium leading-none">
            {t("form.fields.isSubjectSpecific")}
          </p>
          <p className="text-xs text-muted-foreground">
            {t("form.fields.isSubjectSpecificDescription")}
          </p>
          <p className="text-sm text-muted-foreground mt-1">
            {formData.isSubjectSpecific ? t("table.yes") : t("table.no")}
          </p>
        </div>
      </Label>

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

