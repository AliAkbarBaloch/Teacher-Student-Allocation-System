import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AlertCircle, Loader2 } from "lucide-react";
import type {
  SubjectCategory,
  CreateSubjectCategoryRequest,
  UpdateSubjectCategoryRequest,
} from "../types/subjectCategory.types";

interface SubjectCategoryFormProps {
  subjectCategory?: SubjectCategory | null;
  onSubmit: (data: CreateSubjectCategoryRequest | UpdateSubjectCategoryRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function SubjectCategoryForm({
  subjectCategory,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: SubjectCategoryFormProps) {
  const { t } = useTranslation("subjectCategories");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState(() => {
    if (subjectCategory) {
      return {
        categoryTitle: subjectCategory.categoryTitle || "",
      };
    }
    return {
      categoryTitle: "",
    };
  });
  const [errors, setErrors] = useState<Partial<Record<keyof CreateSubjectCategoryRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (subjectCategory) {
      setFormData({
        categoryTitle: subjectCategory.categoryTitle || "",
      });
    } else {
      setFormData({
        categoryTitle: "",
      });
    }
    setErrors({});
  }, [subjectCategory]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateSubjectCategoryRequest, string>> = {};

    if (!formData.categoryTitle.trim()) {
      newErrors.categoryTitle = t("form.errors.titleRequired");
    } else if (formData.categoryTitle.length < 4) {
      newErrors.categoryTitle = t("form.errors.titleMinLength");
    } else if (formData.categoryTitle.length > 255) {
      newErrors.categoryTitle = t("form.errors.titleMaxLength");
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
      if (subjectCategory) {
        // For updates
        const updateData: UpdateSubjectCategoryRequest = {
          categoryTitle: formData.categoryTitle.trim(),
        };
        
        await onSubmit(updateData);
      } else {
        // For create
        const createData: CreateSubjectCategoryRequest = {
          categoryTitle: formData.categoryTitle.trim(),
        };
        await onSubmit(createData);
      }
    } catch {
      // Error handling is done by parent component
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateSubjectCategoryRequest, value: string) => {
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

      <div className="space-y-2">
        <label htmlFor="categoryTitle" className="text-sm font-medium">
          {t("form.fields.title")}
          <span className="text-destructive ml-1">*</span>
        </label>
        <Input
          id="categoryTitle"
          value={formData.categoryTitle}
          onChange={(e) => handleChange("categoryTitle", e.target.value)}
          placeholder={t("form.placeholders.title")}
          disabled={isLoading || isSubmitting}
          className={errors.categoryTitle ? "border-destructive" : ""}
          maxLength={255}
        />
        {errors.categoryTitle && (
          <p className="text-sm text-destructive">{errors.categoryTitle}</p>
        )}
        <p className="text-xs text-muted-foreground">
          {t("form.fields.titleDescription")}
        </p>
      </div>

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
          ) : subjectCategory ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}

