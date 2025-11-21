import { useTranslation } from "react-i18next";
import type { ColumnConfig } from "@/types/datatable.types";

export function useSubjectCategoriesColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("subjectCategories");

  return [
    {
      field: "categoryTitle",
      title: t("table.title"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.title"),
      width: "25%",
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
      width: "25%",
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
      width: "25%",
    },
  ];
}

