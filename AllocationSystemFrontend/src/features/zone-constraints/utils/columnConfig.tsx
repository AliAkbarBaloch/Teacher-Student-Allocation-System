import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { ReactNode } from "react";

// Column configuration for Zone Constraint DataTable

export function useZoneConstraintsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("zoneConstraints");

  return [
    {
      field: "zoneNumber",
      title: t("table.zoneNumber"),
      enableSorting: true,
      fieldType: "number",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.zoneNumber"),
      width: "120px",
      maxWidth: "160px",
    },
    {
      field: "internshipTypeCode",
      title: t("table.internshipTypeCode"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.internshipTypeCode"),
      width: "120px",
      maxWidth: "200px",
    },
    {
      field: "isAllowed",
      title: t("table.isAllowed"),
      enableSorting: true,
      fieldType: "text",
      format: (value: unknown): ReactNode => {
        const allowed = typeof value === "boolean" ? value : false;
        return allowed ? (
          <Badge variant="success">{t("table.allowed")}</Badge>
        ) : (
          <Badge variant="destructive">{t("table.notAllowed")}</Badge>
        );
      },
      width: "120px",
      maxWidth: "160px",
    },
    {
      field: "description",
      title: t("table.description"),
      enableSorting: false,
      fieldType: "text",
      fieldRequired: false,
      fieldPlaceholder: t("form.placeholders.description"),
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
    }
  ];
}