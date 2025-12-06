import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for AllocationPlans DataTable

export function useAllocationPlansColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("allocationPlans");

  return [
    {
      field: "planName",
      title: t("table.planName"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.planName"),
    },
    {
      field: "planVersion",
      title: t("table.planVersion"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.planVersion"),
      width: "120px",
      maxWidth: "180px",
    },
    {
      field: "yearName",
      title: t("table.yearName"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.yearName"),
      width: "160px",
      maxWidth: "200px",
    },
    {
      field: "statusDisplayName",
      title: t("table.status"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode => {
        const status = typeof value === "string" ? value : "";
        let variant: "default" | "success" | "destructive" | "secondary" | "muted" | "outline" | null | undefined = "default";
        if (status === "Approved") variant = "success";
        else if (status === "Draft") variant = "default";
        else if (status === "In Review" || status === "Archived") variant = "destructive";
        return <Badge variant={variant}>{status}</Badge>;
      },
      width: "120px",
      maxWidth: "160px",
    },
    {
      field: "isCurrent",
      title: t("table.isCurrent"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode => {
        const isCurrent = typeof value === "boolean" ? value : false;
        return isCurrent ? (
          <Badge variant="success">{t("table.current")}</Badge>
        ) : (
          <Badge variant="default">{t("table.notCurrent")}</Badge>
        );
      },
      width: "100px",
      maxWidth: "140px",
    },
    {
      field: "createdByUserName",
      title: t("table.createdBy"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: false,
      fieldPlaceholder: t("form.placeholders.createdBy"),
      width: "160px",
      maxWidth: "200px",
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
      width: "160px",
      maxWidth: "200px",
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      enableSorting: true,
      fieldType: "date",
      format: (value: unknown) =>
        typeof value === "string" ? new Date(value).toLocaleString() : "-",
      width: "160px",
      maxWidth: "200px",
    },
  ];
}