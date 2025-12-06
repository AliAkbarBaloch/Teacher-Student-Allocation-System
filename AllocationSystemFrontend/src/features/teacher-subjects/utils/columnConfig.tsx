import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for Teacher Subjects DataTable

export function useTeacherSubjectsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teacherSubjects");

  return [
    {
      field: "academicYearTitle",
      title: t("table.academicYear"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.academicYear"),
    },
    {
      field: "teacherTitle",
      title: t("table.teacher"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.teacher"),
    },
    {
      field: "subjectTitle",
      title: t("table.subjectCode"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.subjectCode"),
    },
    {
      field: "availabilityStatus",
      title: t("table.availabilityStatus"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode => {
        switch (value) {
          case "AVAILABLE":
            return <Badge variant="success">{t("table.available")}</Badge>;
          case "NOT_AVAILABLE":
            return <Badge variant="destructive">{t("table.notAvailable")}</Badge>;
          case "LIMITED":
            return <Badge variant="destructive">{t("table.limited")}</Badge>;
          case "PREFERRED":
            return <Badge variant="default">{t("table.preferred")}</Badge>;
          default:
            return <Badge>{String(value)}</Badge>;
        }
      },
    },
    {
      field: "gradeLevelFrom",
      title: t("table.gradeLevelFrom"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) => value !== null && value !== undefined ? String(value) : "-",
    },
    {
      field: "gradeLevelTo",
      title: t("table.gradeLevelTo"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) => value !== null && value !== undefined ? String(value) : "-",
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
    },
  ];
}