import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import type {
  ZoneConstraint,
  CreateZoneConstraintRequest,
  UpdateZoneConstraintRequest,
} from "../types/zoneConstraint.types";
import { ZoneConstraintForm } from "./ZoneConstraintForm";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface ZoneConstraintDialogsProps {
  // Dialog states
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  isEditDialogOpen: boolean;
  setIsEditDialogOpen: (open: boolean) => void;
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isDeleteDialogOpen: boolean;
  setIsDeleteDialogOpen: (open: boolean) => void;

  // Data
  selectedZoneConstraint: ZoneConstraint | null;

  // Handlers
  onCreateSubmit: (data: CreateZoneConstraintRequest | UpdateZoneConstraintRequest) => Promise<void>;
  onUpdateSubmit: (data: CreateZoneConstraintRequest | UpdateZoneConstraintRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (zoneConstraint: ZoneConstraint) => void;
  onSelectedChange: (zoneConstraint: ZoneConstraint | null) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"zoneConstraints">;
}

export function ZoneConstraintDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedZoneConstraint,
  onCreateSubmit,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
  t,
}: ZoneConstraintDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            <ZoneConstraintForm
              onSubmit={onCreateSubmit}
              onCancel={() => setIsCreateDialogOpen(false)}
              isLoading={isSubmitting}
            />
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* View Dialog (Read-only) */}
      <ViewDialog
        open={isViewDialogOpen}
        onOpenChange={(open) => {
          setIsViewDialogOpen(open);
          if (!open) {
            onSelectedChange(null);
          }
        }}
        data={selectedZoneConstraint}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedZoneConstraint) {
            onEditClick(selectedZoneConstraint);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(zoneConstraint) => (
          <DialogBody>
            <div className="space-y-4 py-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.zoneNumber")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.zoneNumber}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.internshipTypeName")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.internshipTypeName ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.isAllowed")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.isAllowed ? t("table.allowed") : t("table.notAllowed")}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.description")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.description ?? "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.createdAt")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.createdAt
                      ? new Date(zoneConstraint.createdAt).toLocaleString()
                      : "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">{t("form.fields.updatedAt")}</label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {zoneConstraint.updatedAt
                      ? new Date(zoneConstraint.updatedAt).toLocaleString()
                      : "-"}
                  </div>
                </div>
              </div>
            </div>
          </DialogBody>
        )}
      />

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {selectedZoneConstraint && (
              <ZoneConstraintForm
                key={`edit-${selectedZoneConstraint.id}`}
                zoneConstraint={selectedZoneConstraint}
                onSubmit={onUpdateSubmit}
                onCancel={() => {
                  setIsEditDialogOpen(false);
                  onSelectedChange(null);
                }}
                isLoading={isSubmitting}
              />
            )}
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <DeleteConfirmationDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={onDelete}
        title={t("delete.title")}
        description={t("delete.message")}
        cancelLabel={t("delete.cancel")}
        confirmLabel={t("delete.confirm")}
        isSubmitting={isSubmitting}
      />
    </>
  );
}