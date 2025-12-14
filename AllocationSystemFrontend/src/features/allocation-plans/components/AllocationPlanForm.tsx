import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { AcademicYearService, type AcademicYear } from "@/features/academic-years";
import { AlertCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  PlanStatus,
  UpdateAllocationPlanRequest,
} from "../types/allocationPlan.types";

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
    try {
      if (allocationPlan) {
        const updateData: UpdateAllocationPlanRequest = {
          planName: formData.planName.trim(),
          status: formData.status,
          isCurrent: formData.isCurrent,
          notes: formData.notes,
        };
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
        <div className="space-y-2 col-span-1">
          <Label className="text-sm font-medium">
            {t("form.fields.yearId")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={yearValue}
            onValueChange={(value) => {
              if (value === "__none__") {
                handleChange("yearId", 0);
              } else {
                const id = parseInt(value, 10);
                handleChange("yearId", isNaN(id) ? 0 : id);
              }
            }}
            disabled={isLoading || isSubmitting || loadingYears}
          >
            <SelectTrigger
              className={`w-full ${errors.yearId ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.yearId")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.yearId")}
              </SelectItem>
              {years.map((year) => (
                <SelectItem key={year.id} value={String(year.id)}>
                  {year.yearName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.yearId && (
            <p className="text-sm text-destructive">{errors.yearId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="planName" className="text-sm font-medium">
            {t("form.fields.planName")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="planName"
            value={formData.planName}
            onChange={(e) => handleChange("planName", e.target.value)}
            placeholder={t("form.placeholders.planName")}
            disabled={isLoading || isSubmitting}
            className={errors.planName ? "border-destructive" : ""}
            maxLength={255}
          />
          {errors.planName && (
            <p className="text-sm text-destructive">{errors.planName}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="planVersion" className="text-sm font-medium">
            {t("form.fields.planVersion")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="planVersion"
            value={formData.planVersion}
            onChange={(e) => handleChange("planVersion", e.target.value)}
            placeholder={t("form.placeholders.planVersion")}
            disabled={isLoading || isSubmitting}
            className={errors.planVersion ? "border-destructive" : ""}
            maxLength={100}
          />
          {errors.planVersion && (
            <p className="text-sm text-destructive">{errors.planVersion}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <Label className="text-sm font-medium">
            {t("form.fields.status")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={statusValue}
            onValueChange={(value) =>
              handleChange("status", value as PlanStatus)
            }
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger
              className={`w-full ${errors.status ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.status")} />
            </SelectTrigger>
            <SelectContent>
              {PLAN_STATUS_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.status && (
            <p className="text-sm text-destructive">{errors.status}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isCurrent"
              checked={!!formData.isCurrent}
              onCheckedChange={(checked) =>
                handleChange("isCurrent", checked === true)
              }
              disabled={isLoading || isSubmitting}
              className="h-5 w-5"
            />
            <Label
              htmlFor="isCurrent"
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
            >
              {t("form.fields.isCurrent")}
            </Label>
          </div>
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="notes" className="text-sm font-medium">
            {t("form.fields.notes")}
          </Label>
          <Textarea
            id="notes"
            value={formData.notes ?? ""}
            onChange={(e) => handleChange("notes", e.target.value)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            className={errors.notes ? "border-destructive" : ""}
            maxLength={5000}
            rows={3}
          />
          {errors.notes && (
            <p className="text-sm text-destructive">{errors.notes}</p>
          )}
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-2">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </Button>
        <Button type="submit" disabled={isLoading || isSubmitting}>
          {isSubmitting || isLoading
            ? tCommon("actions.saving")
            : allocationPlan
            ? tCommon("actions.update")
            : tCommon("actions.create")}
        </Button>
      </div>
    </form>
  );
}
