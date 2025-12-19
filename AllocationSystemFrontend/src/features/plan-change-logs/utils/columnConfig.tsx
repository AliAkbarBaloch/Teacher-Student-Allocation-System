import { useTranslation } from "react-i18next";
import type { ColumnConfig } from "@/types/datatable.types";

/**
 * Column configuration for PlanChangeLogs DataTable
 */
export function usePlanChangeLogsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("planChangeLogs");

  return [
    {
      field: "id",
      title: t("table.id"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "planId",
      title: t("table.planId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "changeType",
      title: t("table.changeType"),
      enableSorting: true,
      fieldType: "text",
    },
    {
      field: "entityType",
      title: t("table.entityType"),
      enableSorting: true,
      fieldType: "text",
    },
    {
      field: "entityId",
      title: t("table.entityId"),
      enableSorting: true,
      fieldType: "number",
    },
    {
      field: "oldValue",
      title: t("table.oldValue"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "newValue",
      title: t("table.newValue"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "reason",
      title: t("table.reason"),
      enableSorting: false,
      fieldType: "text",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) => (typeof value === "string" && value) || "-",
    },
  ];
}