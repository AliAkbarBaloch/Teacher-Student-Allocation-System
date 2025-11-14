import React from "react";
import { ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Input } from "@/components/ui/input";
import type { Table } from "@tanstack/react-table";

interface DataTableToolbarProps<TData> {
  table: Table<TData>;
  enableSearch?: boolean;
  searchKey?: string;
  searchPlaceholder?: string;
  enableColumnVisibility?: boolean;
}

export function DataTableToolbar<TData>({
  table,
  enableSearch = true,
  searchKey,
  searchPlaceholder = "Search...",
  enableColumnVisibility = true,
}: DataTableToolbarProps<TData>) {
  if (!enableSearch && !enableColumnVisibility) {
    return null;
  }

  return (
    <div className="flex flex-col gap-4 py-4 sm:flex-row sm:items-center">
      {enableSearch && searchKey && (
        <Input
          placeholder={searchPlaceholder}
          value={(table.getColumn(searchKey)?.getFilterValue() as string) ?? ""}
          onChange={(event) =>
            table.getColumn(searchKey)?.setFilterValue(event.target.value)
          }
          className="w-full max-w-sm"
        />
      )}
      {enableColumnVisibility && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" className="w-full sm:ml-auto sm:w-auto">
              Columns <ChevronDown />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {table
              .getAllColumns()
              .filter((column) => column.getCanHide())
              .map((column) => {
                const header = column.columnDef.header;
                let columnTitle: string = column.id;

                // Extract title from header
                if (typeof header === "string") {
                  columnTitle = header;
                } else if (header && typeof header === "function") {
                  // For function headers, try to get a readable name
                  columnTitle = column.id
                    .replace(/([A-Z])/g, " $1")
                    .replace(/^./, (str: string) => str.toUpperCase())
                    .trim();
                }

                // Format column title: capitalize and replace underscores
                columnTitle = columnTitle
                  .replace(/_/g, " ")
                  .replace(/\b\w/g, (char: string) => char.toUpperCase());

                return (
                  <DropdownMenuCheckboxItem
                    key={column.id}
                    checked={column.getIsVisible()}
                    onCheckedChange={(value) => column.toggleVisibility(!!value)}
                  >
                    {columnTitle}
                  </DropdownMenuCheckboxItem>
                );
              })}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </div>
  );
}

