import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { ColumnDef } from "@tanstack/react-table";

import { DataTable } from "@/components/common/DataTable";

type Row = { id: string; name: string };

function renderTable(overrides?: Partial<React.ComponentProps<typeof DataTable<Row>>>) {
  const columns: ColumnDef<Row>[] = [
    {
      accessorKey: "name",
      header: "Name",
      cell: (info) => String(info.getValue() ?? ""),
    },
  ];

  const data: Row[] = [
    { id: "1", name: "Alice" },
    { id: "2", name: "Bob" },
  ];

  return render(
    <DataTable<Row>
      columns={columns}
      data={data}
      searchKey="name"
      enableSearch
      enablePagination={false}
      disableInternalDialog
      {...overrides}
    />
  );
}

describe("DataTable", () => {
  it("renders rows and filters by the search input", async () => {
    const user = userEvent.setup();
    renderTable();

    expect(screen.getByText("Name")).toBeInTheDocument();
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText("Search..."), "Ali");
    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.queryByText("Bob")).not.toBeInTheDocument();
  });

  it("wires action buttons to callbacks and stops row-click propagation", async () => {
    const user = userEvent.setup();
    const onView = vi.fn();
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onRowClick = vi.fn();

    renderTable({
      onRowClick,
      actions: { onView, onEdit, onDelete },
    });

    await user.click(screen.getAllByRole("button", { name: "View" })[0]);
    expect(onView).toHaveBeenCalledTimes(1);
    expect(onView).toHaveBeenCalledWith({ id: "1", name: "Alice" });
    expect(onRowClick).not.toHaveBeenCalled();

    await user.click(screen.getAllByRole("button", { name: "Edit" })[0]);
    expect(onEdit).toHaveBeenCalledTimes(1);
    expect(onEdit).toHaveBeenCalledWith({ id: "1", name: "Alice" });

    await user.click(screen.getAllByRole("button", { name: "Delete" })[0]);
    expect(onDelete).toHaveBeenCalledTimes(1);
    expect(onDelete).toHaveBeenCalledWith({ id: "1", name: "Alice" });
  });

  it("shows empty state message when there are no rows", () => {
    renderTable({ data: [], emptyMessage: "No results." });
    expect(screen.getByText("No results.")).toBeInTheDocument();
  });

  it("shows error message when provided", () => {
    renderTable({ error: "Something went wrong" });
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
  });
});

