import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import type { CreditHourTracking, UpdateCreditHourTrackingRequest } from "../types/creditHourTracking.types";
import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import { Loader2 } from "lucide-react";
import { NOTES_CONSTRAINTS } from "../constants/creditHourTracking.constants";

interface CreditHourTrackingDialogsProps {
  dialogs: {
    view: {
      isOpen: boolean;
      setIsOpen: (open: boolean) => void;
    };
    edit: {
      isOpen: boolean;
      setIsOpen: (open: boolean) => void;
    };
    delete: {
      isOpen: boolean;
      setIsOpen: (open: boolean) => void;
    };
  };
  selectedEntry: CreditHourTracking | null;
  onUpdateSubmit: (id: number, data: UpdateCreditHourTrackingRequest) => Promise<void>;
  onDelete: () => void;
  onEditClick: (entry: CreditHourTracking) => void;
  onSelectedChange: (entry: CreditHourTracking | null) => void;
  isSubmitting: boolean;
}

export function CreditHourTrackingDialogs({
  dialogs,
  selectedEntry,
  onUpdateSubmit,
  onDelete,
  onEditClick,
  onSelectedChange,
  isSubmitting,
}: CreditHourTrackingDialogsProps) {
  const { t } = useTranslation("creditHourTracking");
  const { t: tCommon } = useTranslation("common");
  const [notes, setNotes] = useState("");
  const [isEditingNotes, setIsEditingNotes] = useState(false);

  // Initialize notes when entry changes
  useEffect(() => {
    if (selectedEntry) {
      setNotes(selectedEntry.notes || "");
      setIsEditingNotes(false);
    }
  }, [selectedEntry]);

  const handleSaveNotes = async () => {
    if (!selectedEntry) return;
    await onUpdateSubmit(selectedEntry.id, { notes: notes || null });
    setIsEditingNotes(false);
  };

  const handleCancelNotes = () => {
    if (selectedEntry) {
      setNotes(selectedEntry.notes || "");
      setIsEditingNotes(false);
    }
  };

  return (
    <>
      {/* View Dialog */}
      <ViewDialog
        open={dialogs.view.isOpen}
        onOpenChange={(open) => {
          dialogs.view.setIsOpen(open);
          if (!open) {
            onSelectedChange(null);
            setIsEditingNotes(false);
          }
        }}
        data={selectedEntry}
        title={t("form.title.view")}
        description={t("subtitle")}
        maxWidth="2xl"
        onEdit={() => {
          dialogs.view.setIsOpen(false);
          if (selectedEntry) {
            onEditClick(selectedEntry);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(entry: CreditHourTracking) => (
          <DialogBody>
            <div className="space-y-6 py-4">
              {/* Teacher and Year Info */}
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.teacher")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {entry.teacherName || "-"}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.academicYear")}
                  </label>
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                    {entry.academicYearTitle || "-"}
                  </div>
                </div>
              </div>

              {/* Credit Hours Info */}
              <div className="grid gap-4 md:grid-cols-3">
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.assignmentsCount")}
                  </label>
                  <div className="text-sm font-semibold p-2 border rounded-md bg-muted/50">
                    {entry.assignmentsCount ?? 0}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.creditHoursAllocated")}
                  </label>
                  <div className="text-sm font-semibold p-2 border rounded-md bg-muted/50">
                    {(entry.creditHoursAllocated ?? 0).toFixed(2)}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">
                    {t("form.fields.creditBalance")}
                  </label>
                  <div
                    className={`text-sm font-semibold p-2 border rounded-md ${
                      (entry.creditBalance ?? 0) < 0
                        ? "bg-destructive/10 text-destructive border-destructive/20"
                        : (entry.creditBalance ?? 0) > 50
                        ? "bg-green-500/10 text-green-600 border-green-500/20"
                        : "bg-muted/50"
                    }`}
                  >
                    {(entry.creditBalance ?? 0).toFixed(2)}
                  </div>
                </div>
              </div>

              {/* Notes Section */}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label className="text-sm font-medium">
                    {t("form.fields.notes")}
                  </Label>
                  {!isEditingNotes && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setIsEditingNotes(true)}
                      className="h-7 text-xs"
                    >
                      {t("form.editNotes")}
                    </Button>
                  )}
                </div>
                {isEditingNotes ? (
                  <div className="space-y-2">
                    <Textarea
                      value={notes}
                      onChange={(e) => {
                        const newValue = e.target.value;
                        if (newValue.length <= NOTES_CONSTRAINTS.MAX_LENGTH) {
                          setNotes(newValue);
                        }
                      }}
                      placeholder={t("form.placeholders.notes")}
                      className="min-h-[100px]"
                      disabled={isSubmitting}
                      maxLength={NOTES_CONSTRAINTS.MAX_LENGTH}
                    />
                    {notes.length > NOTES_CONSTRAINTS.MAX_LENGTH * 0.9 && (
                      <p className="text-xs text-muted-foreground">
                        {notes.length} / {NOTES_CONSTRAINTS.MAX_LENGTH} characters
                      </p>
                    )}
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        onClick={handleSaveNotes}
                        disabled={isSubmitting}
                        className="h-8"
                      >
                        {isSubmitting && (
                          <Loader2 className="mr-2 h-3 w-3 animate-spin" />
                        )}
                        {tCommon("actions.save")}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={handleCancelNotes}
                        disabled={isSubmitting}
                        className="h-8"
                      >
                        {tCommon("actions.cancel")}
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 min-h-[60px]">
                    {entry.notes || (
                      <span className="italic">{t("form.noNotes")}</span>
                    )}
                  </div>
                )}
              </div>

              {/* Timestamps */}
              <div className="grid gap-4 md:grid-cols-2 pt-2 border-t">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-muted-foreground">
                    {t("form.fields.createdAt")}
                  </label>
                  <div className="text-xs text-muted-foreground">
                    {entry.createdAt
                      ? new Date(entry.createdAt).toLocaleString()
                      : "-"}
                  </div>
                </div>
                {entry.updatedAt && (
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.updatedAt")}
                    </label>
                    <div className="text-xs text-muted-foreground">
                      {new Date(entry.updatedAt).toLocaleString()}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </DialogBody>
        )}
      />

      {/* Edit Dialog - Currently only for notes, can be extended */}
      <Dialog open={dialogs.edit.isOpen} onOpenChange={dialogs.edit.setIsOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {selectedEntry && (
                <div>
                  <Label htmlFor="notes">{t("form.fields.notes")}</Label>
                  <Textarea
                    id="notes"
                    value={notes}
                    onChange={(e) => {
                      const newValue = e.target.value;
                      if (newValue.length <= NOTES_CONSTRAINTS.MAX_LENGTH) {
                        setNotes(newValue);
                      }
                    }}
                    placeholder={t("form.placeholders.notes")}
                    className="min-h-[100px] mt-2"
                    disabled={isSubmitting}
                    maxLength={NOTES_CONSTRAINTS.MAX_LENGTH}
                  />
                  {notes.length > NOTES_CONSTRAINTS.MAX_LENGTH * 0.9 && (
                    <p className="text-xs text-muted-foreground mt-1">
                      {notes.length} / {NOTES_CONSTRAINTS.MAX_LENGTH} characters
                    </p>
                  )}
                </div>
            )}
          </DialogBody>
          <DialogFooter className="p-3">
            <Button
              variant="outline"
              onClick={() => {
                dialogs.edit.setIsOpen(false);
                handleCancelNotes();
                onSelectedChange(null);
              }}
              disabled={isSubmitting}
            >
              {tCommon("actions.cancel")}
            </Button>
            <Button
              onClick={async () => {
                if (selectedEntry) {
                  try {
                    await handleSaveNotes();
                    // Only close dialog if save was successful
                    dialogs.edit.setIsOpen(false);
                    onSelectedChange(null);
                  } catch {
                    // Error already handled in hook
                  }
                }
              }}
              disabled={isSubmitting || !selectedEntry}
            >
              {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {tCommon("actions.save")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <DeleteConfirmationDialog
        open={dialogs.delete.isOpen}
        onOpenChange={dialogs.delete.setIsOpen}
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
