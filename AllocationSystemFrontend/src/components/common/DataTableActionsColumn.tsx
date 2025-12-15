import { Eye, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
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
    size: 120,
    minSize: 100,
    maxSize: 150,
    enableResizing: false,
    cell: ({ row }) => {
      const rowData = row.original;

      return (
        <div className="flex items-center gap-1">
          {actions.onView && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 sm:h-8 sm:w-8"
                  onClick={(e) => {
                    e.stopPropagation();
                    onView(rowData);
                  }}
                >
                  <Eye className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
                  <span className="sr-only">{actions.labels?.view || "View"}</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{actions.labels?.view || "View"}</p>
              </TooltipContent>
            </Tooltip>
          )}
          {actions.onEdit && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 sm:h-8 sm:w-8"
                  onClick={(e) => {
                    e.stopPropagation();
                    onEdit(rowData);
                  }}
                >
                  <Pencil className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
                  <span className="sr-only">{actions.labels?.edit || "Edit"}</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{actions.labels?.edit || "Edit"}</p>
              </TooltipContent>
            </Tooltip>
          )}
          {actions.customActions &&
            actions.customActions.length > 0 &&
            actions.customActions.map((customAction, index) => {
              const label =
                typeof customAction.label === "function"
                  ? customAction.label(rowData)
                  : customAction.label;
              const icon =
                typeof customAction.icon === "function"
                  ? customAction.icon(rowData)
                  : customAction.icon;

              return (
                <Tooltip key={index}>
                  <TooltipTrigger asChild>
                    <Button
                      variant="ghost"
                      size="icon"
                      className={`h-7 w-7 sm:h-8 sm:w-8 ${customAction.className || ""}`}
                      onClick={(e) => {
                        e.stopPropagation();
                        customAction.onClick(rowData);
                      }}
                    >
                      {icon && (
                        <span className="h-3.5 w-3.5 sm:h-4 sm:w-4 flex items-center">
                          {icon}
                        </span>
                      )}
                      <span className="sr-only">{label}</span>
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p>{label}</p>
                  </TooltipContent>
                </Tooltip>
              );
            })}
          {actions.onDelete && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 sm:h-8 sm:w-8 text-destructive hover:text-destructive"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(rowData);
                  }}
                >
                  <Trash2 className="h-3.5 w-3.5 sm:h-4 sm:w-4" />
                  <span className="sr-only">{actions.labels?.delete || "Delete"}</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{actions.labels?.delete || "Delete"}</p>
              </TooltipContent>
            </Tooltip>
          )}
        </div>
      );
    },
  } as ColumnDef<TData, TValue>;
}

