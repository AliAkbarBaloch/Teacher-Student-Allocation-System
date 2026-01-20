import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import type { Column, Table } from "@tanstack/react-table";

import { DataTableToolbar } from "@/components/common/DataTableToolbar";

type TestRow = { name: string };

describe("DataTableToolbar", () => {
  it("renders search input and calls setFilterValue when typing", async () => {
    const user = userEvent.setup();
    const setFilterValue = vi.fn();

    const column: Pick<
      Column<TestRow, unknown>,
      "getFilterValue" | "setFilterValue"
    > = {
      getFilterValue: vi.fn(() => ""),
      setFilterValue,
    };

    const table: Pick<Table<TestRow>, "getColumn" | "getAllColumns"> = {
      getColumn: vi.fn(() => column as Column<TestRow, unknown>),
      getAllColumns: vi.fn(() => [] as Column<TestRow, unknown>[]),
    };

    render(
      <DataTableToolbar
        table={table as unknown as Table<TestRow>}
        enableSearch
        searchKey="name"
        searchPlaceholder="Search names..."
        enableColumnVisibility={false}
      />
    );

    await user.type(screen.getByPlaceholderText("Search names..."), "Ali");
    expect(setFilterValue).toHaveBeenCalled();
    // Note: our mock keeps the input value controlled at "", so each keypress is reported individually.
    expect(setFilterValue).toHaveBeenCalledTimes(3);
    expect(setFilterValue).toHaveBeenLastCalledWith("i");
  });

  it("renders column visibility menu and toggles visibility", async () => {
    const user = userEvent.setup();
    const toggleVisibility = vi.fn();

    const column = {
      id: "name",
      getCanHide: () => true,
      getIsVisible: () => true,
      toggleVisibility,
      columnDef: { header: "Name" },
    } as unknown as Column<TestRow, unknown>;

    const table: Pick<Table<TestRow>, "getColumn" | "getAllColumns"> = {
      getColumn: vi.fn(() => undefined),
      getAllColumns: vi.fn(() => [column]),
    };

    render(
      <DataTableToolbar
        table={table as unknown as Table<TestRow>}
        enableSearch={false}
        enableColumnVisibility
      />
    );

    await user.click(screen.getByRole("button", { name: /columns/i }));
    await user.click(screen.getByText("Name"));

    expect(toggleVisibility).toHaveBeenCalledWith(false);
  });
});

