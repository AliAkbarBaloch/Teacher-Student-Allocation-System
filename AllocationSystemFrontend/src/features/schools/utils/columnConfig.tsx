import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { School } from "@/features/schools/types/school.types";

export function useSchoolsColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("schools");

  return [
    {
      field: "schoolName",
      title: t("table.columns.name"),
      enableSorting: true,
      format: (value: unknown, row?: unknown) => {
        const school = row as School;
        return (
          <div className="min-w-0">
            <p className="font-medium truncate" title={String(value || "")}>
              {String(value || "")}
            </p>
            {school.address && (
              <p
                className="text-xs text-muted-foreground truncate"
                title={school.address}
              >
                {school.address}
              </p>
            )}
          </div>
        );
      },
    },
    {
      field: "schoolType",
      title: t("table.columns.type"),
      enableSorting: true,
      format: (value: unknown) => {
        return (
          <Badge variant="outline" className="uppercase text-xs">
            {t(`typeLabels.${String(value || "")}`)}
          </Badge>
        );
      },
    },
    {
      field: "zoneNumber",
      title: t("table.columns.zone"),
      enableSorting: true,
      format: (value: unknown) => {
        return <span className="font-medium">{String(value ?? "")}</span>;
      },
    },
    {
      field: "contactEmail",
      title: t("table.columns.contactEmail"),
      enableSorting: false,
      format: (value: unknown) => {
        const email = String(value || "");
        if (!email) {
          return <span className="text-muted-foreground">—</span>;
        }
        return (
          <a
            href={`mailto:${email}`}
            className="text-primary underline-offset-2 hover:underline truncate block"
            title={email}
          >
            {email}
          </a>
        );
      },
    },
    {
      field: "contactPhone",
      title: t("table.columns.contactPhone"),
      enableSorting: false,
      format: (value: unknown) => {
        const phone = String(value || "");
        if (!phone) {
          return <span className="text-muted-foreground">—</span>;
        }
        return (
          <a
            href={`tel:${phone}`}
            className="text-primary underline-offset-2 hover:underline"
          >
            {phone}
          </a>
        );
      },
    },
    {
      field: "isActive",
      title: t("table.columns.status"),
      enableSorting: true,
      format: (value: unknown) => {
        return (
          <Badge variant={typeof value === "boolean" && value ? "success" : "secondary"}>
            {typeof value === "boolean" && value ? t("status.active") : t("status.inactive")}
          </Badge>
        );
      },
    },
  ];
}
