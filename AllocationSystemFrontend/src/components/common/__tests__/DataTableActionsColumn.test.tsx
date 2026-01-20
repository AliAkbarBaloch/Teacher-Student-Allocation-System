import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { CellContext, ColumnDef, Row as TanStackRow } from "@tanstack/react-table";

import { createActionsColumn } from "@/components/common/DataTableActionsColumn";

type TestRow = { id: string; name: string };

describe("createActionsColumn", () => {
  it("calls onView/onEdit/onDelete with row data and stops propagation", async () => {
    const user = userEvent.setup();
    const onView = vi.fn();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    const rowData: TestRow = { id: "1", name: "Alice" };
    const onRowClick = vi.fn();

    const column: ColumnDef<TestRow, unknown> = createActionsColumn<TestRow, unknown>({
      actions: { onView: vi.fn(), onEdit: vi.fn(), onDelete: vi.fn() },
      onView,
      onEdit,
      onDelete,
    });

    const Cell = column.cell as (ctx: CellContext<TestRow, unknown>) => React.ReactNode;

    // Provide the minimal shape that Cell expects from TanStack Table
    const fakeRow: Pick<TanStackRow<TestRow>, "original"> = {
      original: rowData,
    };

    render(
      <div onClick={onRowClick}>
        <Cell
          // Other properties of CellContext are not used by our cell, so we can safely stub them.
          {...({ row: fakeRow } as CellContext<TestRow, unknown>)}
        />
      </div>
    );

    await user.click(screen.getByRole("button", { name: "View" }));
    expect(onView).toHaveBeenCalledWith(rowData);
    expect(onRowClick).not.toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "Edit" }));
    expect(onEdit).toHaveBeenCalledWith(rowData);

    await user.click(screen.getByRole("button", { name: "Delete" }));
    expect(onDelete).toHaveBeenCalledWith(rowData);
  });
});

