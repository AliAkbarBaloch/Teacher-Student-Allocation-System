// react
import { useEffect, useMemo, useState } from "react";
// translations
import { useTranslation } from "react-i18next";

// types
import type {
  CreateSubjectRequest,
  Subject,
  UpdateSubjectRequest,
} from "../types/subject.types";
// components
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { SelectField } from "@/components/form/fields/SelectField";
import useSubjectCategories from "@/hooks/entities/useSubjectCategories";
import { TextField } from "@/components/form/fields/TextField";


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
  const { data: categories = [], isLoading: loadingCategories } = useSubjectCategories();
  const [errors, setErrors] = useState<Partial<Record<keyof CreateSubjectRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  
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

  const schoolTypeValue = useMemo((): string => {
    return formData.schoolType || "__none__";
  }, [formData.schoolType]);

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

      <div className="grid gap-4 md:grid-cols-2">
        <TextField
          id="subjectCode"
          label={t("form.fields.code")}
          value={formData.subjectCode}
          onChange={(val: string) => handleChange("subjectCode", val)}
          placeholder={t("form.placeholders.code")}
          disabled={isLoading || isSubmitting}
          error={errors.subjectCode}
          maxLength={50}
        />

        <TextField
          id="subjectTitle"
          label={t("form.fields.title")}
          value={formData.subjectTitle}
          onChange={(val: string) => handleChange("subjectTitle", val)}
          placeholder={t("form.placeholders.title")}
          disabled={isLoading || isSubmitting}
          error={errors.subjectTitle}
          maxLength={255}
        />

        <SelectField
          id="subjectCategoryId"
          label={t("form.fields.category")}
          value={categoryValue}
          onChange={val => {
            if (val === "__none__") {
              handleChange("subjectCategoryId", 0);
            } else {
              const categoryId = parseInt(val, 10);
              handleChange("subjectCategoryId", !isNaN(categoryId) && categoryId > 0 ? categoryId : 0);
            }
          }}
          options={[
            {
              value: "__none__",
              label: t("form.placeholders.selectCategory"),
            },
            ...(categories.length === 0 && !loadingCategories
              ? [{
                  value: "__none__",
                  label: t("form.placeholders.noCategories"),
                  disabled: true,
                }]
              : categories.map((category) => ({
                  value: String(category.id),
                  label: category.categoryTitle,
                }))
            ),
          ]}
          placeholder={t("form.placeholders.category")}
          required
          error={errors.subjectCategoryId}
          disabled={isLoading || isSubmitting || loadingCategories}
        />

        <SelectField
          id="schoolType"
          label={t("form.fields.schoolType")}
          value={schoolTypeValue}
          onChange={val => {
            if (val === "__none__") {
              handleChange("schoolType", "");
            } else {
              handleChange("schoolType", val);
            }
          }}
          options={[
            { value: "__none__", label: t("form.placeholders.schoolType") },
            { value: "Primary", label: t("table.primary") },
            { value: "Middle", label: t("table.middle") },
          ]}
          placeholder={t("form.placeholders.schoolType")}
          error={errors.schoolType}
          disabled={isLoading || isSubmitting}
        />
      </div>

      <CheckboxField
        id="isActive"
        checked={formData.isActive}
        onCheckedChange={val => handleChange("isActive", val)}
        label={t("form.fields.isActive")}
        description={t("form.fields.isActiveDescription")}
        disabled={isLoading || isSubmitting}
      />

      <div className="flex justify-end gap-2 pt-4">
        <CancelButton
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </CancelButton>
        <SubmitButton
          isLoading={isSubmitting || isLoading}
          isEdit={!!subject}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}

