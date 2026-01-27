import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

const EMPTY_CELL = "-";

type AcademicYearNumberField =
  | "totalCreditHours"
  | "elementarySchoolHours"
  | "middleSchoolHours";

/**
 * Formats an ISO date string for display in the table.
 */
function formatDateTimeCell(value: unknown): string {
  if (typeof value !== "string") {
    return EMPTY_CELL;
  }
  return new Date(value).toLocaleString();
}

/**
 * Renders a badge showing whether the academic year is locked.
 */
function renderLockedBadge(value: unknown, t: (key: string) => string): ReactNode {
  const isLocked = typeof value === "boolean" ? value : false;

  if (isLocked) {
    return <Badge variant="destructive">{t("table.locked")}</Badge>;
  }

  return <Badge variant="success">{t("table.unlocked")}</Badge>;
}

function makeNumberColumn(t: (key: string) => string, field: AcademicYearNumberField): ColumnConfig {
  return {
    field,
    title: t(`table.${field}`),
    enableSorting: true,
    fieldType: "number",
    fieldRequired: true,
    fieldPlaceholder: t(`form.placeholders.${field}`),
  };
}

function getAcademicYearsBaseColumns(t: (key: string) => string): ColumnConfig[] {
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
    makeNumberColumn(t, "totalCreditHours"),
    makeNumberColumn(t, "elementarySchoolHours"),
    makeNumberColumn(t, "middleSchoolHours"),
  ];
}

function getAcademicYearsDateColumns(t: (key: string) => string): ColumnConfig[] {
  return [
    {
      field: "budgetAnnouncementDate",
      title: t("table.budgetAnnouncementDate"),
      enableSorting: true,
      fieldType: "date",
      format: formatDateTimeCell,
    },
    {
      field: "allocationDeadline",
      title: t("table.allocationDeadline"),
      enableSorting: true,
      fieldType: "date",
      format: formatDateTimeCell,
    },
  ];
}

function getAcademicYearsStatusColumn(t: (key: string) => string): ColumnConfig[] {
  return [
    {
      field: "isLocked",
      title: t("table.isLocked"),
      enableSorting: true,
      format: (value: unknown) => renderLockedBadge(value, t),
    },
  ];
}

function buildAcademicYearsColumns(t: (key: string) => string): ColumnConfig[] {
  return [
    ...getAcademicYearsBaseColumns(t),
    ...getAcademicYearsDateColumns(t),
    ...getAcademicYearsStatusColumn(t),
  ];
}

/**
 * Column configuration for AcademicYears DataTable.
 */
export function useAcademicYearsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("academicYears");
  return buildAcademicYearsColumns(t);
}
