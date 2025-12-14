import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for TeacherAssignments DataTable

export function useTeacherAssignmentsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teacherAssignments");

  return [
    {
      field: "planId",
      title: t("table.planId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "teacherId",
      title: t("table.teacherId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "internshipTypeId",
      title: t("table.internshipTypeId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "subjectId",
      title: t("table.subjectId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "studentGroupSize",
      title: t("table.studentGroupSize"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "assignmentStatus",
      title: t("table.assignmentStatus"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode => {
        const status = typeof value === "string" ? value : "";
        let variant: "default" | "success" | "secondary" | "destructive" | "muted" | "outline" | null | undefined = "default";
        if (status === "CONFIRMED") variant = "success";
        else if (status === "CANCELLED") variant = "secondary";
        else if (status === "ON_HOLD") variant = "muted";
        return (
          <Badge variant={variant}>
            {t(`form.status.${status.toLowerCase()}`)}
          </Badge>
        );
      },
    },
    {
      field: "isManualOverride",
      title: t("table.isManualOverride"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode =>
        value ? t("table.yes") : t("table.no"),
    },
    {
      field: "notes",
      title: t("table.notes"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "assignedAt",
      title: t("table.assignedAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
  ];
}