import type { FieldConfig } from "@/components/common/types/form.types";
import type { Subject } from "../types/subject.types";
import type { TFunction } from "i18next";
import { SubjectCategoryService } from "@/features/subject-categories";
import type { SubjectCategory } from "@/features/subject-categories/types/subjectCategory.types";

/**
 * Field configuration for Subject form and view
 * This configuration is used by both GenericForm and ViewDialog
 */
export function getSubjectFieldConfig(
  t: TFunction<"subjects">
): FieldConfig<Subject>[] {
  return [
    {
      name: "subjectCode",
      type: "text",
      label: t("form.fields.code"),
      placeholder: t("form.placeholders.code"),
      required: true,
      validation: {
        required: true,
        minLength: 1,
        maxLength: 50,
      },
      maxLength: 50,
      viewFormat: (value) => (value as string) || "—",
    },
    {
      name: "subjectTitle",
      type: "text",
      label: t("form.fields.title"),
      placeholder: t("form.placeholders.title"),
      required: true,
      validation: {
        required: true,
        minLength: 2,
        maxLength: 255,
      },
      maxLength: 255,
      viewFormat: (value) => (value as string) || "—",
    },
    {
      name: "subjectCategoryId",
      type: "select",
      label: t("form.fields.category"),
      placeholder: t("form.placeholders.selectCategory"),
      required: true,
      validation: {
        required: true,
        custom: (value) => {
          if (!value || value === 0) {
            return t("form.errors.categoryRequired");
          }
          return null;
        },
      },
      // Async options loader
      options: async (): Promise<
        Array<{ value: string | number; label: string }>
      > => {
        try {
          const categories = await SubjectCategoryService.getAll();
          return categories.map((cat: SubjectCategory) => ({
            value: cat.id,
            label: cat.categoryTitle,
          }));
        } catch (error) {
          console.error("Failed to load subject categories:", error);
          return [];
        }
      },
      viewFormat: (_value, data) => {
        return data.subjectCategoryTitle || "—";
      },
    },
    {
      name: "schoolType",
      type: "text",
      label: t("form.fields.schoolType"),
      placeholder: t("form.placeholders.schoolType"),
      required: false,
      validation: {
        maxLength: 50,
      },
      maxLength: 50,
      transform: {
        output: (value: unknown) => {
          if (!value || typeof value !== "string") return null;
          const trimmed = value.trim();
          return trimmed === "" ? null : trimmed;
        },
      },
      viewFormat: (value) => {
        if (!value || typeof value !== "string") return "—";
        return value;
      },
    },
    {
      name: "isActive",
      type: "checkbox",
      label: t("form.fields.isActive"),
      description: t("form.fields.isActiveDescription"),
      required: false,
      viewFormat: (_value, data) => {
        return data.isActive ? t("table.active") : t("table.inactive");
      },
    },
  ];
}

