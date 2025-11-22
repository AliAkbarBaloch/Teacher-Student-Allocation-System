import { MoreHorizontal, Eye, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import type { DataTableActions } from "@/types/datatable.types";
import type { ColumnDef } from "@tanstack/react-table";

interface DataTableActionsColumnProps<TData> {
  actions: DataTableActions<TData>;
  actionsHeader?: string;
  onView: (row: TData) => void;
  onEdit: (row: TData) => void;
  onDelete: (row: TData) => void;
}

/**
 * Creates the actions column for DataTable
 */
export function createActionsColumn<TData, TValue>(
  props: DataTableActionsColumnProps<TData>
): ColumnDef<TData, TValue> {
  const { actions, actionsHeader = "Actions", onView, onEdit, onDelete } =
    props;

  return {
    id: "actions",
    enableHiding: true,
    enableSorting: false,
    header: actionsHeader,
    cell: ({ row }) => {
      const rowData = row.original;

      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-7 w-7 sm:h-8 sm:w-8 p-0">
              <span className="sr-only">Open menu</span>
              <MoreHorizontal className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {actions.onView && (
              <DropdownMenuItem
                onClick={(e) => {
                  e.stopPropagation();
                  onView(rowData);
                }}
                className="cursor-pointer"
              >
                <Eye className="mr-2 h-4 w-4" />
                {actions.labels?.view || "View"}
              </DropdownMenuItem>
            )}
            {actions.onEdit && (
              <DropdownMenuItem
                onClick={(e) => {
                  e.stopPropagation();
                  onEdit(rowData);
                }}
                className="cursor-pointer"
              >
                <Pencil className="mr-2 h-4 w-4" />
                {actions.labels?.edit || "Edit"}
              </DropdownMenuItem>
            )}
            {actions.customActions && actions.customActions.length > 0 && (
              <>
                {(actions.onView || actions.onEdit) && (
                  <DropdownMenuSeparator />
                )}
                {actions.customActions.map((customAction, index) => (
                  <div key={index}>
                    {customAction.separator && index > 0 && (
                      <DropdownMenuSeparator />
                    )}
                    <DropdownMenuItem
                      onClick={(e) => {
                        e.stopPropagation();
                        customAction.onClick(rowData);
                      }}
                      className={`cursor-pointer ${customAction.className || ""}`}
                    >
                      {customAction.icon && (
                        <span className="mr-2 h-4 w-4 flex items-center">
                          {typeof customAction.icon === "function"
                            ? customAction.icon(rowData)
                            : customAction.icon}
                        </span>
                      )}
                      {typeof customAction.label === "function"
                        ? customAction.label(rowData)
                        : customAction.label}
                    </DropdownMenuItem>
                  </div>
                ))}
              </>
            )}
            {actions.onDelete && (
              <>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(rowData);
                  }}
                  className="cursor-pointer text-destructive focus:text-destructive"
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  {actions.labels?.delete || "Delete"}
                </DropdownMenuItem>
              </>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      );
    },
  } as ColumnDef<TData, TValue>;
}

