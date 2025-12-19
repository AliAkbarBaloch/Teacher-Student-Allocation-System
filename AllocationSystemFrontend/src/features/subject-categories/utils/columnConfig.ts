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
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
    },
  ];
}

