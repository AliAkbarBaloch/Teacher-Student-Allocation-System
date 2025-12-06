// react
import { useState, useCallback } from "react";
// translation
import { useTranslation } from "react-i18next";
// icon
import { Plus } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/common/DataTable";
// hooks
import { useDialogState } from "@/hooks/useDialogState";
// features
import {
  ZoneConstraintDialogs,
  useZoneConstraintPage,
  useZoneConstraintsColumnConfig,
} from "@/features/zone-constraints";
// types
import type { ZoneConstraint } from "@/features/zone-constraints/types/zoneConstraint.types";

export default function ZoneConstraintPage() {
  const { t } = useTranslation("zoneConstraints");
  const dialogs = useDialogState();
  const [zoneConstraintToDelete, setZoneConstraintToDelete] = useState<ZoneConstraint | null>(null);

  const {
    zoneConstraints,
    loading,
    error,
    selectedZoneConstraint,
    setSelectedZoneConstraint,
    isSubmitting,
    handleCreate: handleCreateInternal,
    handleUpdate: handleUpdateInternal,
    handleDelete: handleDeleteInternal,
  } = useZoneConstraintPage();

  const columnConfig = useZoneConstraintsColumnConfig();

  const handleCreate = useCallback(
    async (data: Parameters<typeof handleCreateInternal>[0]) => {
      try {
        await handleCreateInternal(data);
        dialogs.create.setIsOpen(false);
      } catch {
        // Error already handled in hook
      }
    },
    [handleCreateInternal, dialogs.create]
  );

  const handleUpdate = useCallback(
    async (data: Parameters<typeof handleUpdateInternal>[0]) => {
      if (!selectedZoneConstraint) return;
      try {
        await handleUpdateInternal(data, selectedZoneConstraint.id);
        dialogs.edit.setIsOpen(false);
        setSelectedZoneConstraint(null);
      } catch {
        // Error already handled in hook
      }
    },
    [handleUpdateInternal, selectedZoneConstraint, setSelectedZoneConstraint, dialogs.edit]
  );

  const handleDelete = useCallback(async () => {
    if (!zoneConstraintToDelete) return;
    try {
      await handleDeleteInternal(zoneConstraintToDelete.id);
      dialogs.delete.setIsOpen(false);
      setZoneConstraintToDelete(null);
    } catch {
      // Error already handled in hook
    }
  }, [handleDeleteInternal, zoneConstraintToDelete, dialogs.delete]);

  const handleEditClick = useCallback((zoneConstraint: ZoneConstraint) => {
    setSelectedZoneConstraint(zoneConstraint);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedZoneConstraint, dialogs.edit]);

  const handleDeleteClick = useCallback((zoneConstraint: ZoneConstraint) => {
    setZoneConstraintToDelete(zoneConstraint);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((zoneConstraint: ZoneConstraint) => {
    setSelectedZoneConstraint(zoneConstraint);
    dialogs.view.setIsOpen(true);
  }, [setSelectedZoneConstraint, dialogs.view]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">{t("title")}</h2>
          <p className="text-muted-foreground text-sm mt-1">{t("subtitle")}</p>
        </div>
        <Button onClick={() => dialogs.create.setIsOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          {t("actions.create")}
        </Button>
      </div>

      <DataTable
        columnConfig={columnConfig}
        data={zoneConstraints}
        searchKey="zoneNumber"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={true}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        actions={{
          onView: handleViewClick,
          onEdit: handleEditClick,
          onDelete: handleDeleteClick,
          labels: {
            view: t("actions.view"),
            edit: t("actions.edit"),
            delete: t("actions.delete"),
          },
        }}
      />

      <ZoneConstraintDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedZoneConstraint={selectedZoneConstraint}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedZoneConstraint}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}