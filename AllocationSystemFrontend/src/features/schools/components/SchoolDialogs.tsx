import { Loader2 } from "lucide-react";
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { SchoolForm } from "./SchoolForm";
import { SchoolLocationMap } from "./SchoolLocationMap";
import { SchoolStatusBadge } from "./SchoolStatusBadge";
import type { School, CreateSchoolRequest, UpdateSchoolRequest } from "../types/school.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface SchoolDialogsProps {
  // Dialog states
  isCreateDialogOpen: boolean;
  setIsCreateDialogOpen: (open: boolean) => void;
  isEditDialogOpen: boolean;
  setIsEditDialogOpen: (open: boolean) => void;
  isViewDialogOpen: boolean;
  setIsViewDialogOpen: (open: boolean) => void;
  isStatusDialogOpen: boolean;
  setIsStatusDialogOpen: (open: boolean) => void;
  isDeleteDialogOpen: boolean;
  setIsDeleteDialogOpen: (open: boolean) => void;

  // Data
  selectedSchool: School | null;
  formLoading: boolean;
  statusTarget: { school: School | null; nextState: boolean };
  deleteTarget: School | null;

  // Handlers
  onCreateSubmit: (payload: CreateSchoolRequest) => Promise<void>;
  onUpdateSubmit: (payload: UpdateSchoolRequest) => Promise<void>;
  onStatusChange: () => Promise<void>;
  onDelete: () => Promise<void>;
  onOpenEdit: (school: School) => Promise<void>;
  onStatusTargetChange: (target: { school: School | null; nextState: boolean }) => void;

  // States
  isSubmitting: boolean;

  // Translations
  t: TFunction<"schools">;
}

export function SchoolDialogs({
  isCreateDialogOpen,
  setIsCreateDialogOpen,
  isEditDialogOpen,
  setIsEditDialogOpen,
  isViewDialogOpen,
  setIsViewDialogOpen,
  isStatusDialogOpen,
  setIsStatusDialogOpen,
  isDeleteDialogOpen,
  setIsDeleteDialogOpen,
  selectedSchool,
  formLoading,
  statusTarget,
  onCreateSubmit,
  onUpdateSubmit,
  onStatusChange,
  onDelete,
  onOpenEdit,
  onStatusTargetChange,
  isSubmitting,
  t,
}: SchoolDialogsProps) {
  const { t: tCommon } = useTranslation("common");
  return (
    <>
      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            <SchoolForm
              mode="create"
              onSubmit={onCreateSubmit}
              onCancel={() => setIsCreateDialogOpen(false)}
              isSubmitting={isSubmitting}
            />
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.edit")}</DialogTitle>
            <DialogDescription>{t("form.description")}</DialogDescription>
          </DialogHeader>
          <DialogBody>
            {formLoading ? (
              <div className="flex min-h-[200px] items-center justify-center">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : (
              selectedSchool && (
                <SchoolForm
                  mode="edit"
                  school={selectedSchool}
                  onSubmit={onUpdateSubmit}
                  onCancel={() => setIsEditDialogOpen(false)}
                  isSubmitting={isSubmitting}
                />
              )
            )}
          </DialogBody>
        </DialogContent>
      </Dialog>

      {/* View Dialog */}
      <ViewDialog
        open={isViewDialogOpen}
        onOpenChange={setIsViewDialogOpen}
        data={selectedSchool}
        title={t("form.title.view")}
        description={t("form.description")}
        maxWidth="2xl"
        onEdit={() => {
          setIsViewDialogOpen(false);
          if (selectedSchool) {
            onOpenEdit(selectedSchool);
          }
        }}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(school) => (
          <DialogBody>
            <div className="grid gap-4">
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.schoolName")}</p>
                <p className="text-base">{school.schoolName}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.schoolType")}</p>
                <p>{t(`typeLabels.${school.schoolType}`)}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.zoneNumber")}</p>
                <p>{school.zoneNumber}</p>
              </div>
              {school.address && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.address")}</p>
                  <p className="whitespace-pre-line">{school.address}</p>
                </div>
              )}
              {school.transportAccessibility && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">
                    {t("form.fields.transportAccessibility")}
                  </p>
                  <p className="whitespace-pre-line">{school.transportAccessibility}</p>
                </div>
              )}
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {school.latitude && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.latitude")}</p>
                    <p>{school.latitude}</p>
                  </div>
                )}
                {school.longitude && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.longitude")}</p>
                    <p>{school.longitude}</p>
                  </div>
                )}
                {school.distanceFromCenter && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.distanceFromCenter")}
                    </p>
                    <p>{school.distanceFromCenter}</p>
                  </div>
                )}
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.isActive")}</p>
                  <SchoolStatusBadge isActive={school.isActive} />
                </div>
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {school.contactEmail && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.contactEmail")}
                    </p>
                    <a
                      href={`mailto:${school.contactEmail}`}
                      className="text-primary underline-offset-2 hover:underline"
                    >
                      {school.contactEmail}
                    </a>
                  </div>
                )}
                {school.contactPhone && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">
                      {t("form.fields.contactPhone")}
                    </p>
                    <a
                      href={`tel:${school.contactPhone}`}
                      className="text-primary underline-offset-2 hover:underline"
                    >
                      {school.contactPhone}
                    </a>
                  </div>
                )}
              </div>
              {/* Location Map - at the bottom */}
              {(school.latitude || school.longitude) && (
                <div className="grid gap-1">
                  <p className="text-sm font-medium text-muted-foreground">Location Map</p>
                  <SchoolLocationMap
                    latitude={school.latitude}
                    longitude={school.longitude}
                    schoolName={school.schoolName}
                    className="w-full"
                  />
                </div>
              )}
            </div>
          </DialogBody>
        )}
      />

      {/* Activate/Deactivate Confirmation */}
      <AlertDialog
        open={isStatusDialogOpen}
        onOpenChange={(open) => {
          setIsStatusDialogOpen(open);
          if (!open) {
            onStatusTargetChange({ school: null, nextState: false });
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {statusTarget.nextState ? t("status.activateTitle") : t("status.deactivateTitle")}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {statusTarget.nextState ? t("status.activateDescription") : t("status.deactivateDescription")}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isSubmitting}>{t("actions.cancel")}</AlertDialogCancel>
            <AlertDialogAction
              onClick={onStatusChange}
              disabled={isSubmitting || !statusTarget.school}
              className={statusTarget.nextState ? "" : "bg-destructive text-white hover:bg-destructive/90"}
            >
              {isSubmitting ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : statusTarget.nextState ? (
                t("actions.activate")
              ) : (
                t("actions.deactivate")
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Delete Confirmation */}
      <DeleteConfirmationDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        onConfirm={onDelete}
        title={t("delete.title")}
        description={t("delete.description")}
        cancelLabel={t("delete.cancel")}
        confirmLabel={t("delete.confirm")}
        isSubmitting={isSubmitting}
      />
    </>
  );
}

