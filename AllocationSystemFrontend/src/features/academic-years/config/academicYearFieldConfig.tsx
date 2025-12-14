import type { FieldConfig } from "@/components/common/types/form.types";
import type { AcademicYear } from "../types/academicYear.types";
import type { TFunction } from "i18next";

/**
 * Field configuration for AcademicYear form and view
 * This configuration is used by both GenericForm and ViewDialog
 */
export function getAcademicYearFieldConfig(
  t: TFunction<"academicYears">
): FieldConfig<AcademicYear>[] {
  return [
    {
      name: "yearName",
      type: "text",
      label: t("form.fields.yearName"),
      placeholder: t("form.placeholders.yearName"),
      required: true,
      validation: {
        required: true,
      },
      maxLength: 100,
      viewFormat: (value) => (value as string) || "—",
    },
    {
      name: "totalCreditHours",
      type: "number",
      label: t("form.fields.totalCreditHours"),
      placeholder: t("form.placeholders.totalCreditHours"),
      required: true,
      validation: {
        required: true,
        min: 0,
      },
      min: 0,
      viewFormat: (value) => String(value ?? "—"),
    },
    {
      name: "elementarySchoolHours",
      type: "number",
      label: t("form.fields.elementarySchoolHours"),
      placeholder: t("form.placeholders.elementarySchoolHours"),
      required: true,
      validation: {
        required: true,
        min: 0,
      },
      min: 0,
      viewFormat: (value) => String(value ?? "—"),
    },
    {
      name: "middleSchoolHours",
      type: "number",
      label: t("form.fields.middleSchoolHours"),
      placeholder: t("form.placeholders.middleSchoolHours"),
      required: true,
      validation: {
        required: true,
        min: 0,
      },
      min: 0,
      viewFormat: (value) => String(value ?? "—"),
    },
    {
      name: "budgetAnnouncementDate",
      type: "datetime-local",
      label: t("form.fields.budgetAnnouncementDate"),
      placeholder: t("form.placeholders.budgetAnnouncementDate"),
      required: true,
      validation: {
        required: true,
      },
      transform: {
        // Convert ISO string to datetime-local format for input
        input: (value: unknown) => {
          if (!value || typeof value !== "string") return "";
          // Convert ISO string to datetime-local format (YYYY-MM-DDTHH:mm)
          const date = new Date(value);
          if (isNaN(date.getTime())) return "";
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, "0");
          const day = String(date.getDate()).padStart(2, "0");
          const hours = String(date.getHours()).padStart(2, "0");
          const minutes = String(date.getMinutes()).padStart(2, "0");
          return `${year}-${month}-${day}T${hours}:${minutes}`;
        },
        // Convert datetime-local format back to ISO string
        output: (value: unknown) => {
          if (!value || typeof value !== "string") return "";
          return new Date(value).toISOString();
        },
      },
      viewFormat: (value: unknown) => {
        if (!value || typeof value !== "string") return "—";
        try {
          return new Date(value).toLocaleString();
        } catch {
          return String(value);
        }
      },
    },
    {
      name: "allocationDeadline",
      type: "datetime-local",
      label: t("form.fields.allocationDeadline"),
      placeholder: t("form.placeholders.allocationDeadline"),
      required: false,
      transform: {
        input: (value: unknown) => {
          if (!value || typeof value !== "string") return "";
          const date = new Date(value);
          if (isNaN(date.getTime())) return "";
          const year = date.getFullYear();
          const month = String(date.getMonth() + 1).padStart(2, "0");
          const day = String(date.getDate()).padStart(2, "0");
          const hours = String(date.getHours()).padStart(2, "0");
          const minutes = String(date.getMinutes()).padStart(2, "0");
          return `${year}-${month}-${day}T${hours}:${minutes}`;
        },
        output: (value: unknown) => {
          if (!value || typeof value !== "string") return null;
          return new Date(value).toISOString();
        },
      },
      viewFormat: (value: unknown) => {
        if (!value || typeof value !== "string") return "—";
        try {
          return new Date(value).toLocaleString();
        } catch {
          return String(value);
        }
      },
    },
    {
      name: "isLocked",
      type: "checkbox",
      label: t("form.fields.isLocked"),
      description: t("form.fields.isLockedDescription"),
      required: false,
      viewFormat: (_value, data) => {
        return data.isLocked ? t("table.locked") : t("table.unlocked");
      },
    },
    {
      name: "createdAt",
      type: "text",
      label: t("form.fields.createdAt"),
      required: false,
      showInForm: false, // Don't show in form, only in view
      viewFormat: (value: unknown) => {
        if (!value || typeof value !== "string") return "—";
        try {
          return new Date(value).toLocaleString();
        } catch {
          return String(value);
        }
      },
    },
    {
      name: "updatedAt",
      type: "text",
      label: t("form.fields.updatedAt"),
      required: false,
      showInForm: false, // Don't show in form, only in view
      viewFormat: (value: unknown) => {
        if (!value || typeof value !== "string") return "—";
        try {
          return new Date(value).toLocaleString();
        } catch {
          return String(value);
        }
      },
    },
  ];
}
