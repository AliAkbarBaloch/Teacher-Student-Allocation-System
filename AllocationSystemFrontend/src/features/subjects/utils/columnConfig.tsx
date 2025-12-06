import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for Subjects DataTable

export function useSubjectsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("subjects");

  return [
    {
      field: "subjectCode",
      title: t("table.code"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.code"),
    },
    {
      field: "subjectTitle",
      title: t("table.title"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.title"),
    },
    {
      field: "subjectCategoryTitle",
      title: t("table.category"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "schoolType",
      title: t("table.schoolType"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "isActive",
      title: t("table.status"),
      enableSorting: true,
      format: (value: unknown): ReactNode => {
        const isActive = typeof value === "boolean" ? value : false;
        return isActive ? (
          <Badge variant="success">
            {t("table.active")}
          </Badge>
        ) : (
          <Badge variant="secondary">{t("table.inactive")}</Badge>
        );
      },
    },
  ];
}

