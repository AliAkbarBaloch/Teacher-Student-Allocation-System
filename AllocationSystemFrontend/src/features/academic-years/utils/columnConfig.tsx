import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for AcademicYears DataTable

export function useAcademicYearsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("academicYears");

  return [
    {
      field: "yearName",
      title: t("table.yearName"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.yearName"),
      width: "180px",
      maxWidth: "220px",
    },
    {
      field: "totalCreditHours",
      title: t("table.totalCreditHours"),
      enableSorting: true,
      fieldType: "number",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.totalCreditHours"),
    },
    {
      field: "elementarySchoolHours",
      title: t("table.elementarySchoolHours"),
      enableSorting: true,
      fieldType: "number",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.elementarySchoolHours"),
    },
    {
      field: "middleSchoolHours",
      title: t("table.middleSchoolHours"),
      enableSorting: true,
      fieldType: "number",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.middleSchoolHours"),
    },
    {
      field: "budgetAnnouncementDate",
      title: t("table.budgetAnnouncementDate"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
    },
    {
      field: "allocationDeadline",
      title: t("table.allocationDeadline"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
    },
    {
      field: "isLocked",
      title: t("table.isLocked"),
      enableSorting: true,
      format: (value: unknown): ReactNode => {
        const isLocked = typeof value === "boolean" ? value : false;
        return isLocked ? (
          <Badge variant="destructive">{t("table.locked")}</Badge>
        ) : (
          <Badge variant="success">{t("table.unlocked")}</Badge>
        );
      },
    }
  ];
}