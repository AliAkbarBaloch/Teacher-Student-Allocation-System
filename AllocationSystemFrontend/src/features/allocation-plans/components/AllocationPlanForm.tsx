import { useState, useEffect, useMemo } from "react";
// translations
import { useTranslation } from "react-i18next";
// icons
import { AlertCircle } from "lucide-react";

// types
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  PlanStatus,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

// components
import { AcademicYearService, type AcademicYear } from "@/features/academic-years";
import { SelectField } from "@/components/form/fields/SelectField";
import { TextField } from "@/components/form/fields/TextField";
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { TextAreaField } from "@/components/form/fields/TextAreaField";
import { CheckboxField } from "@/components/form/fields/CheckboxField";

// constants
const PLAN_STATUS_OPTIONS: { value: PlanStatus; label: string }[] = [
  { value: "DRAFT", label: "Draft" },
  { value: "IN_REVIEW", label: "In Review" },
  { value: "APPROVED", label: "Approved" },
  { value: "ARCHIVED", label: "Archived" },
];

interface AllocationPlanFormProps {
  allocationPlan?: AllocationPlan | null;
  onSubmit: (
    data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest
  ) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function AllocationPlanForm({
  allocationPlan,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: AllocationPlanFormProps) {
  const { t } = useTranslation("allocationPlans");
  const { t: tCommon } = useTranslation("common");

  // Academic years for dropdown
  const [years, setYears] = useState<AcademicYear[]>([]);
  const [loadingYears, setLoadingYears] = useState(true);

  // Form state
  const [formData, setFormData] = useState<CreateAllocationPlanRequest>(() => {
    if (allocationPlan) {
      return {
        yearId: allocationPlan.yearId,
        planName: allocationPlan.planName || "",
        planVersion: allocationPlan.planVersion || "",
        status: allocationPlan.status,
        isCurrent: allocationPlan.isCurrent ?? false,
        notes: allocationPlan.notes ?? "",
      };
    }
    return {
      yearId: 0,
      planName: "",
      planVersion: "",
      status: "DRAFT",
      isCurrent: false,
      notes: "",
    };
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateAllocationPlanRequest, string>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load academic years for dropdown
  useEffect(() => {
    const loadYears = async () => {
      setLoadingYears(true);
      try {
        const data = await AcademicYearService.getAll();
        setYears(data);
      } catch {
        setYears([]);
      } finally {
        setLoadingYears(false);
      }
    };
    loadYears();
  }, []);

  useEffect(() => {
    if (allocationPlan) {
      setFormData({
        yearId: allocationPlan.yearId,
        planName: allocationPlan.planName || "",
        planVersion: allocationPlan.planVersion || "",
        status: allocationPlan.status,
        isCurrent: allocationPlan.isCurrent ?? false,
        notes: allocationPlan.notes ?? "",
      });
    } else {
      setFormData({
        yearId: 0,
        planName: "",
        planVersion: "",
        status: "DRAFT",
        isCurrent: false,
        notes: "",
      });
    }
    setErrors({});
  }, [allocationPlan]);

  // Normalize select values
  const yearValue = useMemo(
    () => (formData.yearId > 0 ? String(formData.yearId) : "__none__"),
    [formData.yearId]
  );
  const statusValue = useMemo(
    () => formData.status ?? "DRAFT",
    [formData.status]
  );

  // Year options with placeholder
  const yearOptions = useMemo(() => [
    { value: "__none__", label: t("form.placeholders.yearId") },
    ...years.map((year) => ({ value: String(year.id), label: year.yearName })),
  ], [years, t]);

  const validate = (): boolean => {
    const newErrors: Partial<
      Record<keyof CreateAllocationPlanRequest, string>
    > = {};
    if (!formData.yearId || formData.yearId < 1) {
      newErrors.yearId = t("form.errors.yearIdRequired");
    }
    if (!formData.planName.trim()) {
      newErrors.planName = t("form.errors.planNameRequired");
    }
    if (!formData.planVersion.trim()) {
      newErrors.planVersion = t("form.errors.planVersionRequired");
    }
    if (!formData.status) {
      newErrors.status = t("form.errors.statusRequired");
    }
    if (formData.notes && formData.notes.length > 5000) {
      newErrors.notes = t("form.errors.notesMaxLength");
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loadingYears) return;
    if (!validate()) return;
    setIsSubmitting(true);
    // Debug log: print formData and isCurrent value
    console.log("[DEBUG] Submitting Allocation Plan formData:", formData);
    console.log("[DEBUG] isCurrent value:", formData.isCurrent);
    try {
      if (allocationPlan) {
        const updateData: UpdateAllocationPlanRequest = {
          planName: formData.planName.trim(),
          status: formData.status,
          isCurrent: formData.isCurrent,
          notes: formData.notes,
        };
        console.log("[DEBUG] updateData payload:", updateData);
        await onSubmit(updateData);
      } else {
        const createData: CreateAllocationPlanRequest = {
          yearId: formData.yearId,
          planName: formData.planName.trim(),
          planVersion: formData.planVersion.trim(),
          status: formData.status,
          isCurrent: formData.isCurrent,
          notes: formData.notes,
        };
        console.log("[DEBUG] createData payload:", createData);
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    field: keyof CreateAllocationPlanRequest,
    value: string | number | boolean | null
  ) => {
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
          <SelectField
            id="yearId"
            label={t("form.fields.yearId")}
            value={yearValue}
            onChange={(val: string) => handleChange("yearId", val === "__none__" ? 0 : Number(val))}
            options={yearOptions}
            placeholder={t("form.placeholders.yearId")}
            disabled={isLoading || isSubmitting || loadingYears}
            error={errors.yearId}
          />

          <TextField
            id="planName"
            label={t("form.fields.planName")}
            value={formData.planName}
            onChange={(val: string) => handleChange("planName", val)}
            placeholder={t("form.placeholders.planName")}
            disabled={isLoading || isSubmitting}
            error={errors.planName}
            maxLength={255}
          />

          <TextField
            id="planVersion"
            label={t("form.fields.planVersion")}
            value={formData.planVersion}
            onChange={(val: string) => handleChange("planVersion", val)}
            placeholder={t("form.placeholders.planVersion")}
            disabled={isLoading || isSubmitting}
            error={errors.planVersion}
            maxLength={100}
          />

          <SelectField
            id="status"
            label={t("form.fields.status")}
            value={statusValue}
            onChange={(val: string) => handleChange("status", val as PlanStatus)}
            options={PLAN_STATUS_OPTIONS}
            placeholder={t("form.placeholders.status")}
            disabled={isLoading || isSubmitting}
            error={errors.status}
          />

            <CheckboxField
              id="isCurrent"
              label={t("form.fields.isCurrent")}
              checked={!!formData.isCurrent}
              onCheckedChange={(checked: boolean) =>
                handleChange("isCurrent", checked)
              }
              disabled={isLoading || isSubmitting}
              labelClassName="mt-1.5"
              className="lg:mt-7"
            />
              
          <TextAreaField
            id="notes"
            label={t("form.fields.notes")}
            value={formData.notes ?? ""}
            onChange={(val: string) => handleChange("notes", val)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            error={errors.notes}
            maxLength={500}
          />
      </div>

      <div className="flex justify-end gap-2 pt-2">
        <CancelButton
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </CancelButton>
        <SubmitButton
          isLoading={isSubmitting || isLoading}
          isEdit={!!allocationPlan}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}