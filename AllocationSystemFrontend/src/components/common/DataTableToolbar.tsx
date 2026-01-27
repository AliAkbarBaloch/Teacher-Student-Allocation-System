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

/**
 * Props for the DataTableToolbar component.
 */
interface DataTableToolbarProps<TData> {
  table: Table<TData>;
  enableSearch?: boolean;
  searchKey?: string;
  searchPlaceholder?: string;
  enableColumnVisibility?: boolean;
}

/**
 * Sub-component for rendering the search input field.
 */
function SearchInput<TData>({
  table,
  searchKey,
  placeholder,
}: {
  table: Table<TData>;
  searchKey: string;
  placeholder: string;
}) {
  return (
    <Input
      placeholder={placeholder}
      value={(table.getColumn(searchKey)?.getFilterValue() as string) ?? ""}
      onChange={(event) => table.getColumn(searchKey)?.setFilterValue(event.target.value)}
      className="w-full max-w-sm"
    />
  );
}

/**
 * Sub-component for rendering the column visibility toggle dropdown.
 */
function ColumnVisibilityToggle<TData>({ table }: { table: Table<TData> }) {
  const getColumnTitle = (column: any) => {
    const header = column.columnDef.header;
    let title: string = column.id;

    if (typeof header === "string") {
      title = header;
    } else if (header && typeof header === "function") {
      title = column.id
        .replace(/([A-Z])/g, " $1")
        .replace(/^./, (str: string) => str.toUpperCase())
        .trim();
    }

    return title
      .replace(/_/g, " ")
      .replace(/\b\w/g, (char: string) => char.toUpperCase());
  };

  return (
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
          .map((column) => (
            <DropdownMenuCheckboxItem
              key={column.id}
              checked={column.getIsVisible()}
              onCheckedChange={(value) => column.toggleVisibility(!!value)}
            >
              {getColumnTitle(column)}
            </DropdownMenuCheckboxItem>
          ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

/**
 * Renders the toolbar for the DataTable, including a search input and column visibility toggle.
 */
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
        <SearchInput
          table={table}
          searchKey={searchKey}
          placeholder={searchPlaceholder}
        />
      )}
      {enableColumnVisibility && (
        <ColumnVisibilityToggle table={table} />
      )}
    </div>
  );
}
