import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for TeacherAvailability DataTable

export function useTeacherAvailabilityColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teacherAvailability");

  return [
    {
      field: "teacherName",
      title: t("table.teacher"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.teacher"),
    },
    {
      field: "academicYearName",
      title: t("table.academicYear"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.academicYear"),
    },
    {
      field: "internshipTypeName",
      title: t("table.internshipType"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.internshipType"),
    },
    {
      field: "isAvailable",
      title: t("table.isAvailable"),
      enableSorting: true,
      format: (value: unknown): ReactNode => {
        const available = typeof value === "boolean" ? value : false;
        return available ? (
          <Badge variant="success">{t("table.available")}</Badge>
        ) : (
          <Badge variant="destructive">{t("table.notAvailable")}</Badge>
        );
      },
    },
    {
      field: "preferenceRank",
      title: t("table.preferenceRank"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) =>
        value !== null && value !== undefined ? String(value) : "-",
    },
    {
      field: "notes",
      title: t("table.notes"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
      width: "200px",
      maxWidth: "300px",
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
    },
  ];
}