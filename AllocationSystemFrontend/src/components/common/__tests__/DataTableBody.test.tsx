import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  getCoreRowModel,
  useReactTable,
  type ColumnDef,
} from "@tanstack/react-table";

import { Table } from "@/components/ui/table";
import { DataTableBody } from "@/components/common/DataTableBody";

type Row = { id: string; name: string };

function BodyHarness(props: {
  data: Row[];
  loading: boolean;
  emptyMessage: string;
  enableRowClick: boolean;
  onRowClick?: (row: Row) => void;
}) {
  const columns = React.useMemo<ColumnDef<Row>[]>(
    () => [
      { accessorKey: "name", header: "Name", cell: (info) => String(info.getValue() ?? "") },
    ],
    []
  );

  const table = useReactTable({
    data: props.data,
    columns,
    getCoreRowModel: getCoreRowModel(),
  });

  return (
    <Table>
      <DataTableBody<Row>
        table={table}
        loading={props.loading}
        emptyMessage={props.emptyMessage}
        enableRowClick={props.enableRowClick}
        onRowClick={props.onRowClick}
      />
    </Table>
  );
}

describe("DataTableBody", () => {
  it("shows loading state", () => {
    render(
      <BodyHarness
        data={[{ id: "1", name: "Alice" }]}
        loading
        emptyMessage="No results"
        enableRowClick={false}
      />
    );

    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("shows empty state when there are no rows", () => {
    render(
      <BodyHarness
        data={[]}
        loading={false}
        emptyMessage="No results"
        enableRowClick={false}
      />
    );

    expect(screen.getByText("No results")).toBeInTheDocument();
  });

  it("calls onRowClick when enabled and row is clicked", async () => {
    const user = userEvent.setup();
    const onRowClick = vi.fn();

    render(
      <BodyHarness
        data={[{ id: "1", name: "Alice" }]}
        loading={false}
        emptyMessage="No results"
        enableRowClick
        onRowClick={onRowClick}
      />
    );

    await user.click(screen.getByText("Alice"));
    expect(onRowClick).toHaveBeenCalledWith({ id: "1", name: "Alice" });
  });
});

