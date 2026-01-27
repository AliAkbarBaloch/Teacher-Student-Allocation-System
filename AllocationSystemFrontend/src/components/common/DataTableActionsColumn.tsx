import React from "react";
import { Eye, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import type { DataTableActions } from "@/types/datatable.types";
import type { ColumnDef } from "@tanstack/react-table";

/**
 * Props for the DataTableActionsColumn component.
 */
interface DataTableActionsColumnProps<TData> {
  actions: DataTableActions<TData>;
  actionsHeader?: string;
  onView: (row: TData) => void;
  onEdit: (row: TData) => void;
  onDelete: (row: TData) => void;
}

interface ActionButtonProps {
  onClick: (e: React.MouseEvent) => void;
  icon: React.ReactNode;
  label: string;
  className?: string;
  variant?: "ghost" | "default" | "destructive" | "outline" | "secondary" | "link";
}

/**
 * A reusable action button with a tooltip.
 */
function ActionButton({ onClick, icon, label, className = "", variant = "ghost" }: ActionButtonProps) {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          variant={variant}
          size="icon"
          className={`h-7 w-7 sm:h-8 sm:w-8 ${className}`}
          onClick={onClick}
        >
          {icon}
          <span className="sr-only">{label}</span>
        </Button>
      </TooltipTrigger>
      <TooltipContent>
        <p>{label}</p>
      </TooltipContent>
    </Tooltip>
  );
}

interface ActionsCellProps<TData> {
  rowData: TData;
  props: DataTableActionsColumnProps<TData>;
}

/**
 * Component for rendering the actions cell in the DataTable.
 */
function ActionsCell<TData>({ rowData, props }: ActionsCellProps<TData>) {
  const { actions, onView, onEdit, onDelete } = props;

  return (
    <div className="flex items-center gap-1">
      {actions.onView && (
        <ActionButton
          icon={<Eye className="h-3.5 w-3.5 sm:h-4 sm:w-4" />}
          label={actions.labels?.view || "View"}
          onClick={(e) => {
            e.stopPropagation();
            onView(rowData);
          }}
        />
      )}
      {actions.onEdit && (
        <ActionButton
          icon={<Pencil className="h-3.5 w-3.5 sm:h-4 sm:w-4" />}
          label={actions.labels?.edit || "Edit"}
          onClick={(e) => {
            e.stopPropagation();
            onEdit(rowData);
          }}
        />
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
            <ActionButton
              key={index}
              icon={
                icon && (
                  <span className="h-3.5 w-3.5 sm:h-4 sm:w-4 flex items-center">
                    {icon}
                  </span>
                )
              }
              label={label || ""}
              className={customAction.className}
              onClick={(e) => {
                e.stopPropagation();
                customAction.onClick(rowData);
              }}
            />
          );
        })}
      {actions.onDelete && (
        <ActionButton
          icon={<Trash2 className="h-3.5 w-3.5 sm:h-4 sm:w-4" />}
          label={actions.labels?.delete || "Delete"}
          variant="ghost"
          className="text-destructive hover:text-destructive"
          onClick={(e) => {
            e.stopPropagation();
            onDelete(rowData);
          }}
        />
      )}
    </div>
  );
}

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
