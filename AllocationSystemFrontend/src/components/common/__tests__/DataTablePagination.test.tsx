import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { DataTablePagination } from "@/components/common/DataTablePagination";

describe("DataTablePagination", () => {
  it("returns null when totalRows is 0 (server-side)", () => {
    const { container } = render(
      <DataTablePagination
        serverSidePagination={{
          page: 1,
          pageSize: 10,
          totalItems: 0,
          totalPages: 0,
          onPageChange: vi.fn(),
          onPageSizeChange: vi.fn(),
        }}
      />
    );

    expect(container).toBeEmptyDOMElement();
  });

  it("renders summary and pages, and calls onPageChange (server-side)", async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();

    render(
      <DataTablePagination
        serverSidePagination={{
          page: 2,
          pageSize: 10,
          totalItems: 35,
          totalPages: 4,
          onPageChange,
          onPageSizeChange: vi.fn(),
        }}
      />
    );

    expect(screen.getByText(/Showing 11 to 20 of 35 results/i)).toBeInTheDocument();

    await user.click(screen.getByText("3"));
    expect(onPageChange).toHaveBeenCalledWith(3);

    await user.click(screen.getByLabelText("Go to previous page"));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it("calls onPageSizeChange when selecting a new page size (server-side)", async () => {
    const user = userEvent.setup();
    const onPageSizeChange = vi.fn();

    render(
      <DataTablePagination
        pageSizeOptions={[10, 25, 50]}
        serverSidePagination={{
          page: 1,
          pageSize: 10,
          totalItems: 35,
          totalPages: 4,
          onPageChange: vi.fn(),
          onPageSizeChange,
        }}
      />
    );

    // Open Radix Select (trigger has role="combobox")
    await user.click(screen.getByRole("combobox"));
    await user.click(await screen.findByRole("option", { name: "25" }));

    expect(onPageSizeChange).toHaveBeenCalledWith(25);
  });
});

