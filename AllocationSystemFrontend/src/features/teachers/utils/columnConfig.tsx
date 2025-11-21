import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { Teacher } from "../types/teacher.types";

export function useTeachersColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("teachers");

  return [
    {
      field: "firstName",
      title: t("table.columns.name"),
      enableSorting: true,
      format: (_value: unknown, row?: unknown) => {
        const teacher = row as Teacher;
        return (
          <div>
            <p className="font-medium">{`${teacher.firstName} ${teacher.lastName}`}</p>
          </div>
        );
      },
    },
    {
      field: "email",
      title: t("table.columns.email"),
      enableSorting: true,
      format: (value: unknown) => {
        const email = typeof value === "string" ? value : "";
        return (
          <a
            href={`mailto:${email}`}
            className="text-primary underline-offset-2 hover:underline break-word"
            onClick={(e) => e.stopPropagation()}
          >
            {email}
          </a>
        );
      },
    },
    {
      field: "schoolName",
      title: t("table.columns.schoolName"),
      enableSorting: true,
      width: "200px",
      maxWidth: "200px",
      format: (_value: unknown, row?: unknown) => {
        const teacher = row as Teacher;
        return (
          <div className="min-w-0">
            <p className="truncate" title={teacher.schoolName || "-"}>
              {teacher.schoolName || "-"}
            </p>
          </div>
        );
      },
    },
    {
      field: "employmentStatus",
      title: t("table.columns.employmentStatus"),
      enableSorting: true,
      format: (_value: unknown, row?: unknown) => {
        const teacher = row as Teacher;
        return (
          <Badge variant="outline" className="uppercase text-xs">
            {t(`form.employmentStatus.${teacher.employmentStatus}`, {
              defaultValue: teacher.employmentStatus,
            })}
          </Badge>
        );
      },
    },
    {
      field: "isPartTime",
      title: t("table.columns.isPartTime"),
      enableSorting: true,
      format: (value: unknown) => {
        const isPartTime = typeof value === "boolean" ? value : false;
        return isPartTime ? (
          <Badge variant="secondary" className="text-xs">
            {t("table.yes")}
          </Badge>
        ) : (
          <Badge variant="outline" className="text-xs">
            {t("table.no")}
          </Badge>
        );
      },
    },
    {
      field: "isActive",
      title: t("table.columns.status"),
      enableSorting: true,
      format: (value: unknown) => {
        const isActive = typeof value === "boolean" ? value : false;
        return isActive ? (
          <Badge variant="default" className="bg-green-500 hover:bg-green-600">
            {t("status.active")}
          </Badge>
        ) : (
          <Badge variant="secondary">{t("status.inactive")}</Badge>
        );
      },
    },
  ];
}

