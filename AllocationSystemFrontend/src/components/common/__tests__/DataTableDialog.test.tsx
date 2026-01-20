import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { DataTableDialog } from "@/components/common/DataTableDialog";
import type { ColumnConfig, DataTableActions } from "@/types/datatable.types";

type Row = { id: string; name: string; role: string };

function renderDialog({
  isEditingInitial = false,
  validationError,
}: {
  isEditingInitial?: boolean;
  validationError?: string | null;
} = {}) {
  const user = userEvent.setup();

  const actions: DataTableActions<Row> = {
    onEdit: vi.fn(),
    onDelete: vi.fn(),
    onUpdate: vi.fn(),
  };

  const onSave = vi.fn(async () => {});
  const onConfirmDelete = vi.fn();

  const columnConfig: ColumnConfig[] = [
    { field: "id", title: "ID", fieldReadOnly: true },
    { field: "name", title: "Name" },
    {
      field: "role",
      title: "Role",
      fieldType: "select",
      fieldOptions: [
        { label: "USER", value: "USER" },
        { label: "ADMIN", value: "ADMIN" },
      ],
    },
  ];

  function Wrapper() {
    const [open, setOpen] = React.useState(true);
    const [isEditing, setIsEditing] = React.useState(isEditingInitial);
    const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
    const [editingRow, setEditingRow] = React.useState<Partial<Row>>({
      id: "1",
      name: "Alice",
      role: "USER",
    });

    return (
      <DataTableDialog<Row>
        open={open}
        onOpenChange={setOpen}
        selectedRow={{ id: "1", name: "Alice", role: "USER" }}
        editingRow={editingRow}
        isEditing={isEditing}
        setIsEditing={setIsEditing}
        setEditingRow={setEditingRow}
        columnConfig={columnConfig}
        actions={actions}
        onSave={onSave}
        onDeleteClick={() => setDeleteDialogOpen(true)}
        deleteDialogOpen={deleteDialogOpen}
        setDeleteDialogOpen={setDeleteDialogOpen}
        onConfirmDelete={onConfirmDelete}
        validationError={validationError}
        isSaving={false}
      />
    );
  }

  render(<Wrapper />);

  return { user, onSave, onConfirmDelete };
}

describe("DataTableDialog", () => {
  it("renders view mode and can switch to edit mode", async () => {
    const { user } = renderDialog({ isEditingInitial: false });

    expect(screen.getByText("View Record")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Delete" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Edit" }));

    expect(screen.getByText("Edit Record")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Save Changes" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeInTheDocument();
  });

  it("disables save when validationError is present", async () => {
    const { user, onSave } = renderDialog({ isEditingInitial: true, validationError: "Invalid" });

    const save = screen.getByRole("button", { name: "Save Changes" });
    expect(save).toBeDisabled();

    await user.click(save);
    expect(onSave).not.toHaveBeenCalled();
  });

  it("calls onSave when Save Changes is clicked", async () => {
    const { user, onSave } = renderDialog({ isEditingInitial: true, validationError: null });

    await user.click(screen.getByRole("button", { name: "Save Changes" }));
    expect(onSave).toHaveBeenCalledTimes(1);
  });

  it("opens delete confirmation and calls onConfirmDelete", async () => {
    const { user, onConfirmDelete } = renderDialog({ isEditingInitial: false });

    await user.click(screen.getByRole("button", { name: "Delete" }));

    const confirmDialog = (await screen.findByText("Are you sure?")) as HTMLElement;
    const dialogRoot = (confirmDialog.closest("[data-state]") ?? document.body) as HTMLElement;
    const scope = within(dialogRoot);

    // Confirm delete button inside the alert dialog
    await user.click(scope.getByRole("button", { name: "Delete" }));
    expect(onConfirmDelete).toHaveBeenCalledTimes(1);
  });
});

