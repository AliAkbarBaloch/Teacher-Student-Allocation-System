import { useTranslation } from "react-i18next";
import type { ColumnConfig } from "@/types/datatable.types";
import {
  planNameColumn,
  planVersionColumn,
  yearNameColumn,
  statusColumns,
  auditColumns,
} from "@/features/allocation-plans/utils/allocationPlans.columns.helper";

export function useAllocationPlansColumnConfig(): ColumnConfig[] {
  const { t } = useTranslation("allocationPlans");

  return [
    planNameColumn(t),
    planVersionColumn(t),
    yearNameColumn(t),
    ...statusColumns(t),
    ...auditColumns(t),
  ];
}
