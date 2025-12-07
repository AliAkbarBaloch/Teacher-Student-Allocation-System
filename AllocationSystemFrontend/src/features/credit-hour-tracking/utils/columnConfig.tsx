import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";
import { CREDIT_BALANCE_THRESHOLDS, NOTES_CONSTRAINTS } from "../constants/creditHourTracking.constants";

// Column configuration for Credit Hour Tracking DataTable

export function useCreditHourTrackingColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("creditHourTracking");

  return [
    {
      field: "teacherName",
      title: t("table.teacher"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      format: (value: unknown) => {
        return value ? String(value) : "-";
      },
    },
    {
      field: "academicYearTitle",
      title: t("table.academicYear"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      format: (value: unknown) => {
        return value ? String(value) : "-";
      },
    },
    {
      field: "assignmentsCount",
      title: t("table.assignmentsCount"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) => String(value ?? 0),
    },
    {
      field: "creditHoursAllocated",
      title: t("table.creditHoursAllocated"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown) => {
        const num = typeof value === "number" ? value : Number(value) || 0;
        return num.toFixed(2);
      },
    },
    {
      field: "creditBalance",
      title: t("table.creditBalance"),
      enableSorting: true,
      fieldType: "number",
      format: (value: unknown): ReactNode => {
        const num = typeof value === "number" ? value : Number(value) || 0;
        const formatted = num.toFixed(2);
        
        // Conditional formatting based on balance value
        if (num < CREDIT_BALANCE_THRESHOLDS.NEGATIVE) {
          // Negative balance - over-utilized (red)
          return (
            <Badge variant="destructive" className="font-semibold">
              {formatted}
            </Badge>
          );
        } else if (num > CREDIT_BALANCE_THRESHOLDS.HIGH) {
          // High positive balance - under-utilized (green)
          return (
            <Badge variant="outline" className="font-semibold bg-green-500/10 text-green-600 border-green-500/20">
              {formatted}
            </Badge>
          );
        } else if (num > CREDIT_BALANCE_THRESHOLDS.MODERATE) {
          // Moderate positive balance (yellow/orange)
          return (
            <Badge variant="outline" className="font-semibold bg-yellow-500/10 text-yellow-600 border-yellow-500/20">
              {formatted}
            </Badge>
          );
        } else {
          // Normal range (default)
          return <span className="font-medium">{formatted}</span>;
        }
      },
    },
    {
      field: "notes",
      title: t("table.notes"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => {
        const notes = String(value || "");
        if (notes.length > NOTES_CONSTRAINTS.DISPLAY_TRUNCATE_LENGTH) {
          return notes.substring(0, NOTES_CONSTRAINTS.DISPLAY_TRUNCATE_LENGTH) + "...";
        }
        return notes || "-";
      },
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleDateString() : "-",
    },
  ];
}
