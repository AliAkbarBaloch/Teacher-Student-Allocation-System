import type { ColumnDef } from "@tanstack/react-table";
import {
  ActionsCell,
  type DataTableActionsColumnProps
} from "./DataTableActionsCell";

/**
 * Creates the actions column for DataTable
 */
export function createActionsColumn<TData, TValue>(
  props: DataTableActionsColumnProps<TData>
): ColumnDef<TData, TValue> {
  const { actionsHeader = "Actions" } = props;

  return {
    id: "actions",
    enableHiding: true,
    enableSorting: false,
    header: actionsHeader,
    enableResizing: false,
    cell: ({ row }) => <ActionsCell rowData={row.original} props={props} />,
  } as ColumnDef<TData, TValue>;
}
