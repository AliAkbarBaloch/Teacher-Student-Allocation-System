import { useTranslation } from "react-i18next";
import type { ColumnConfig } from "@/types/datatable.types";

export function useInternshipTypesColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("internshipTypes");

  return [
    {
      field: "internshipCode",
      title: t("table.code"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.code"),
    },
    {
      field: "fullName",
      title: t("table.fullName"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.fullName"),
      width: "300px",
      maxWidth: "400px",
    },
    {
      field: "timing",
      title: t("table.timing"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "periodType",
      title: t("table.periodType"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "semester",
      title: t("table.semester"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "isSubjectSpecific",
      title: t("table.isSubjectSpecific"),
      enableSorting: true,
      format: (value: unknown) => {
        if (typeof value === "boolean") {
          return value ? t("table.yes") : t("table.no");
        }
        return String(value ?? "");
      },
    },
    {
      field: "priorityOrder",
      title: t("table.priorityOrder"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) => (typeof value === "number" ? value : null) ?? "-",
    },
  ];
}

