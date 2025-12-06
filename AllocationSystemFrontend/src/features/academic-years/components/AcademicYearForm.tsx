import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { AlertCircle, Loader2 } from "lucide-react";
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
          <label htmlFor="yearName" className="text-sm font-medium">
            {t("form.fields.yearName")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="yearName"
            value={formData.yearName}
            onChange={(e) => handleChange("yearName", e.target.value)}
            placeholder={t("form.placeholders.yearName")}
            disabled={isLoading || isSubmitting}
            className={errors.yearName ? "border-destructive" : ""}
            maxLength={100}
          />
          {errors.yearName && (
            <p className="text-sm text-destructive">{errors.yearName}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="totalCreditHours" className="text-sm font-medium">
            {t("form.fields.totalCreditHours")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="totalCreditHours"
            type="number"
            value={formData.totalCreditHours}
            onChange={(e) => handleChange("totalCreditHours", Number(e.target.value))}
            placeholder={t("form.placeholders.totalCreditHours")}
            disabled={isLoading || isSubmitting}
            className={errors.totalCreditHours ? "border-destructive" : ""}
            min={0}
          />
          {errors.totalCreditHours && (
            <p className="text-sm text-destructive">{errors.totalCreditHours}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="elementarySchoolHours" className="text-sm font-medium">
            {t("form.fields.elementarySchoolHours")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="elementarySchoolHours"
            type="number"
            value={formData.elementarySchoolHours}
            onChange={(e) => handleChange("elementarySchoolHours", Number(e.target.value))}
            placeholder={t("form.placeholders.elementarySchoolHours")}
            disabled={isLoading || isSubmitting}
            className={errors.elementarySchoolHours ? "border-destructive" : ""}
            min={0}
          />
          {errors.elementarySchoolHours && (
            <p className="text-sm text-destructive">{errors.elementarySchoolHours}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="middleSchoolHours" className="text-sm font-medium">
            {t("form.fields.middleSchoolHours")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="middleSchoolHours"
            type="number"
            value={formData.middleSchoolHours}
            onChange={(e) => handleChange("middleSchoolHours", Number(e.target.value))}
            placeholder={t("form.placeholders.middleSchoolHours")}
            disabled={isLoading || isSubmitting}
            className={errors.middleSchoolHours ? "border-destructive" : ""}
            min={0}
          />
          {errors.middleSchoolHours && (
            <p className="text-sm text-destructive">{errors.middleSchoolHours}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="budgetAnnouncementDate" className="text-sm font-medium">
            {t("form.fields.budgetAnnouncementDate")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="budgetAnnouncementDate"
            type="datetime-local"
            value={formData.budgetAnnouncementDate}
            onChange={(e) => handleChange("budgetAnnouncementDate", e.target.value)}
            placeholder={t("form.placeholders.budgetAnnouncementDate")}
            disabled={isLoading || isSubmitting}
            className={errors.budgetAnnouncementDate ? "border-destructive" : ""}
          />
          {errors.budgetAnnouncementDate && (
            <p className="text-sm text-destructive">{errors.budgetAnnouncementDate}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="allocationDeadline" className="text-sm font-medium">
            {t("form.fields.allocationDeadline")}
          </label>
          <Input
            id="allocationDeadline"
            type="datetime-local"
            value={formData.allocationDeadline ?? ""}
            onChange={(e) =>
              handleChange("allocationDeadline", e.target.value || null)
            }
            placeholder={t("form.placeholders.allocationDeadline")}
            disabled={isLoading || isSubmitting}
          />
        </div>
      </div>

      <Label
        htmlFor="isLocked"
        className="hover:bg-accent/50 flex items-start gap-3 rounded-lg border p-4 cursor-pointer has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10 transition-colors"
      >
        <Checkbox
          id="isLocked"
          checked={!!formData.isLocked}
          onCheckedChange={(checked) =>
            handleChange("isLocked", checked === true)
          }
          disabled={isLoading || isSubmitting}
          className="h-5 w-5 mt-0.5 data-[state=checked]:border-primary data-[state=checked]:bg-primary"
        />
        <div className="grid gap-1.5 flex-1">
          <p className="text-sm font-medium leading-none">
            {t("form.fields.isLocked")}
          </p>
          <p className="text-xs text-muted-foreground">
            {t("form.fields.isLockedDescription")}
          </p>
          <p className="text-sm text-muted-foreground mt-1">
            {formData.isLocked ? t("table.locked") : t("table.unlocked")}
          </p>
        </div>
      </Label>

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
          {isSubmitting || isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              {tCommon("actions.saving")}
            </>
          ) : academicYear ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}