import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for TeacherAvailability DataTable

export function useTeacherAvailabilityColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teacherAvailability");

  return [
    {
      field: "teacherFirstName",
      title: t("table.teacher"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.teacher"),
      format: (value: unknown, row?: unknown) => {
        if (row && typeof row === "object" && "teacherFirstName" in row && "teacherLastName" in row) {
          const firstName = String(row.teacherFirstName || "");
          const lastName = String(row.teacherLastName || "");
          return `${firstName} ${lastName}`.trim() || "-";
        }
        return String(value || "");
      },
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
      field: "status",
      title: t("table.status"),
      enableSorting: true,
      format: (value: unknown): ReactNode => {
        switch (value) {
          case "AVAILABLE":
            return <Badge variant="success">{t("table.available")}</Badge>;
          case "PREFERRED":
            return <Badge variant="default">{t("table.preferred")}</Badge>;
          case "NOT_AVAILABLE":
            return <Badge variant="destructive">{t("table.notAvailable")}</Badge>;
          case "BACKUP_ONLY":
            return <Badge variant="secondary">{t("table.backupOnly")}</Badge>;
          default:
            return <Badge>{String(value)}</Badge>;
        }
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
  ];
}