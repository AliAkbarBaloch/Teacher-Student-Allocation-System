// react
import { useState, useEffect, useMemo } from "react";
// translations
import { useTranslation } from "react-i18next";
// components
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
// icons
import { AlertCircle, Loader2 } from "lucide-react";
// types
import type {
  Subject,
  CreateSubjectRequest,
  UpdateSubjectRequest,
} from "../types/subject.types";
import type { SubjectCategory } from "@/features/subject-categories/types/subjectCategory.types";
// services
import { SubjectCategoryService } from "@/features/subject-categories/services/subjectCategoryService";


interface SubjectFormProps {
  subject?: Subject | null;
  onSubmit: (data: CreateSubjectRequest | UpdateSubjectRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function SubjectForm({
  subject,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: SubjectFormProps) {
  const { t } = useTranslation("subjects");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState(() => {
    if (subject) {
      return {
        subjectCode: subject.subjectCode || "",
        subjectTitle: subject.subjectTitle || "",
        subjectCategoryId: subject.subjectCategoryId || 0,
        schoolType: subject.schoolType ?? "",
        isActive: subject.isActive ?? true,
      };
    }
    return {
      subjectCode: "",
      subjectTitle: "",
      subjectCategoryId: 0,
      schoolType: "",
      isActive: true,
    };
  });
  const [categories, setCategories] = useState<SubjectCategory[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(true);
  const [errors, setErrors] = useState<Partial<Record<keyof CreateSubjectRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load subject categories
  useEffect(() => {
    const loadCategories = async () => {
      try {
        const data = await SubjectCategoryService.getAll();
        setCategories(data);
        // If no categories exist, show a warning
        if (data.length === 0) {
          console.warn("No subject categories found. Please create categories first.");
        }
      } catch (err) {
        console.error("Failed to load subject categories:", err);
        // Set empty array on error to prevent form from being stuck
        setCategories([]);
      } finally {
        setLoadingCategories(false);
      }
    };
    loadCategories();
  }, []);

  useEffect(() => {
    if (subject) {
      setFormData({
        subjectCode: subject.subjectCode || "",
        subjectTitle: subject.subjectTitle || "",
        subjectCategoryId: subject.subjectCategoryId || 0,
        schoolType: subject.schoolType ?? "",
        isActive: subject.isActive ?? true,
      });
    } else {
      setFormData({
        subjectCode: "",
        subjectTitle: "",
        subjectCategoryId: 0,
        schoolType: "",
        isActive: true,
      });
    }
    setErrors({});
  }, [subject]);

  // Normalize Select values
  const categoryValue = useMemo((): string => {
    return formData.subjectCategoryId > 0 ? String(formData.subjectCategoryId) : "__none__";
  }, [formData.subjectCategoryId]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateSubjectRequest, string>> = {};

    const trimmedCode = formData.subjectCode.trim();
    if (!trimmedCode) {
      newErrors.subjectCode = t("form.errors.codeRequired");
    } else if (trimmedCode.length < 1) {
      newErrors.subjectCode = t("form.errors.codeMinLength");
    } else if (trimmedCode.length > 50) {
      newErrors.subjectCode = t("form.errors.codeMaxLength");
    }

    const trimmedTitle = formData.subjectTitle.trim();
    if (!trimmedTitle) {
      newErrors.subjectTitle = t("form.errors.titleRequired");
    } else if (trimmedTitle.length < 2) {
      newErrors.subjectTitle = t("form.errors.titleMinLength");
    } else if (trimmedTitle.length > 255) {
      newErrors.subjectTitle = t("form.errors.titleMaxLength");
    }

    if (!formData.subjectCategoryId || formData.subjectCategoryId === 0) {
      newErrors.subjectCategoryId = t("form.errors.categoryRequired");
    }

    if (formData.schoolType && formData.schoolType.length > 50) {
      newErrors.schoolType = t("form.errors.schoolTypeMaxLength");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Don't submit if categories are still loading
    if (loadingCategories) {
      return;
    }

    if (!validate()) {
      return;
    }

    // Double-check category ID is valid
    if (!formData.subjectCategoryId || formData.subjectCategoryId === 0 || isNaN(formData.subjectCategoryId)) {
      setErrors({ subjectCategoryId: t("form.errors.categoryRequired") });
      return;
    }

    // Verify the category exists in the loaded categories
    const categoryExists = categories.some(cat => cat.id === formData.subjectCategoryId);
    if (!categoryExists) {
      setErrors({ subjectCategoryId: t("form.errors.categoryInvalid") });
      return;
    }

    setIsSubmitting(true);
    try {
      // Helper to convert empty strings to null for optional fields
      const toOptionalString = (value: string): string | null => {
        const trimmed = value.trim();
        return trimmed === "" ? null : trimmed;
      };

      if (subject) {
        // For updates, send all fields - backend UpdateDto accepts all optional fields
        const updateData: UpdateSubjectRequest = {
          subjectCode: formData.subjectCode.trim(),
          subjectTitle: formData.subjectTitle.trim(),
          subjectCategoryId: formData.subjectCategoryId,
          schoolType: toOptionalString(formData.schoolType),
          isActive: formData.isActive ?? true,
        };
        
        await onSubmit(updateData);
      } else {
        // For create, send all required fields
        // Ensure isActive defaults to true if not set
        const createData: CreateSubjectRequest = {
          subjectCode: formData.subjectCode.trim(),
          subjectTitle: formData.subjectTitle.trim(),
          subjectCategoryId: formData.subjectCategoryId,
          schoolType: toOptionalString(formData.schoolType),
          isActive: formData.isActive ?? true,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateSubjectRequest, value: string | boolean | number | null) => {
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

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2 col-span-1">
          <label htmlFor="subjectCode" className="text-sm font-medium">
            {t("form.fields.code")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="subjectCode"
            value={formData.subjectCode}
            onChange={(e) => handleChange("subjectCode", e.target.value)}
            placeholder={t("form.placeholders.code")}
            disabled={isLoading || isSubmitting}
            className={errors.subjectCode ? "border-destructive" : ""}
            maxLength={50}
          />
          {errors.subjectCode && (
            <p className="text-sm text-destructive">{errors.subjectCode}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="subjectTitle" className="text-sm font-medium">
            {t("form.fields.title")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Input
            id="subjectTitle"
            value={formData.subjectTitle}
            onChange={(e) => handleChange("subjectTitle", e.target.value)}
            placeholder={t("form.placeholders.title")}
            disabled={isLoading || isSubmitting}
            className={errors.subjectTitle ? "border-destructive" : ""}
            maxLength={255}
          />
          {errors.subjectTitle && (
            <p className="text-sm text-destructive">{errors.subjectTitle}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="subjectCategoryId" className="text-sm font-medium">
            {t("form.fields.category")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={categoryValue}
            onValueChange={(value) => {
              if (value === "__none__") {
                handleChange("subjectCategoryId", 0);
              } else {
                const categoryId = parseInt(value, 10);
                if (!isNaN(categoryId) && categoryId > 0) {
                  handleChange("subjectCategoryId", categoryId);
                } else {
                  handleChange("subjectCategoryId", 0);
                }
              }
            }}
            disabled={isLoading || isSubmitting || loadingCategories}
          >
            <SelectTrigger
              className={`w-full ${errors.subjectCategoryId ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={t("form.placeholders.category")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.selectCategory")}
              </SelectItem>
              {categories.length === 0 && !loadingCategories ? (
                <SelectItem value="__none__" disabled>
                  {t("form.placeholders.noCategories")}
                </SelectItem>
              ) : (
                categories.map((category) => (
                  <SelectItem key={category.id} value={String(category.id)}>
                    {category.categoryTitle}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.subjectCategoryId && (
            <p className="text-sm text-destructive">{errors.subjectCategoryId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="schoolType" className="text-sm font-medium">
            {t("form.fields.schoolType")}
          </label>
          <Input
            id="schoolType"
            value={formData.schoolType}
            onChange={(e) => handleChange("schoolType", e.target.value)}
            placeholder={t("form.placeholders.schoolType")}
            disabled={isLoading || isSubmitting}
            className={errors.schoolType ? "border-destructive" : ""}
            maxLength={50}
          />
          {errors.schoolType && (
            <p className="text-sm text-destructive">{errors.schoolType}</p>
          )}
        </div>
      </div>

      <Label
        htmlFor="isActive"
        className="hover:bg-accent/50 flex items-start gap-3 rounded-lg border p-4 cursor-pointer has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10 transition-colors"
      >
        <Checkbox
          id="isActive"
          checked={formData.isActive}
          onCheckedChange={(checked) =>
            handleChange("isActive", checked === true)
          }
          disabled={isLoading || isSubmitting}
          className="h-5 w-5 mt-0.5 data-[state=checked]:border-primary data-[state=checked]:bg-primary"
        />
        <div className="grid gap-1.5 flex-1">
          <p className="text-sm font-medium leading-none">
            {t("form.fields.isActive")}
          </p>
          <p className="text-xs text-muted-foreground">
            {t("form.fields.isActiveDescription")}
          </p>
          <p className="text-sm text-muted-foreground mt-1">
            {formData.isActive ? t("table.active") : t("table.inactive")}
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
          ) : subject ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}

