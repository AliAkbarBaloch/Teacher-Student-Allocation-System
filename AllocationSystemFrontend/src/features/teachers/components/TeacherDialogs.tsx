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
import { Badge } from "@/components/ui/badge";
import { ViewDialog } from "@/components/common/ViewDialog";
import { DeleteConfirmationDialog } from "@/components/common/DeleteConfirmationDialog";
import { TeacherForm } from "./TeacherForm";
import type { Teacher, CreateTeacherRequest, UpdateTeacherRequest } from "../types/teacher.types";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";

interface TeacherDialogsProps {
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
  selectedTeacher: Teacher | null;
  formLoading: boolean;
  createFormKey: number;
  statusTarget: { teacher: Teacher | null; nextState: boolean };
  deleteTarget: Teacher | null;
  warningMessage: string | null;

  // Handlers
  onCreateSubmit: (payload: CreateTeacherRequest) => Promise<void>;
  onUpdateSubmit: (payload: UpdateTeacherRequest) => Promise<void>;
  onStatusChange: () => Promise<void>;
  onDelete: () => Promise<void>;
  onCloseStatus: () => void;
  onEdit?: () => void;

  // States
  isSubmitting: boolean;
  isAdmin: boolean;

  // Translations
  t: TFunction<"teachers">;
}

export function TeacherDialogs({
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
  selectedTeacher,
  formLoading,
  createFormKey,
  statusTarget,
  warningMessage,
  onCreateSubmit,
  onUpdateSubmit,
  onStatusChange,
  onDelete,
  onCloseStatus,
  onEdit,
  isSubmitting,
  isAdmin,
  t,
}: TeacherDialogsProps) {
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
            {isCreateDialogOpen && (
              <TeacherForm
                key={`create-teacher-form-${createFormKey}`}
                mode="create"
                onSubmit={onCreateSubmit}
                onCancel={() => setIsCreateDialogOpen(false)}
                isSubmitting={isSubmitting}
              />
            )}
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
              selectedTeacher && (
                <TeacherForm
                  mode="edit"
                  teacher={selectedTeacher}
                  onSubmit={onUpdateSubmit}
                  onCancel={() => setIsEditDialogOpen(false)}
                  isSubmitting={isSubmitting}
                  readOnly={!isAdmin}
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
        data={selectedTeacher}
        title={t("form.title.view")}
        description={t("form.description")}
        maxWidth="2xl"
        onEdit={onEdit}
        editLabel={tCommon("actions.edit")}
        closeLabel={tCommon("actions.close")}
        renderCustomContent={(teacher) => (
          <DialogBody>
            <div className="grid gap-4">
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.firstName")}</p>
                <p className="text-base">{teacher.firstName}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.lastName")}</p>
                <p className="text-base">{teacher.lastName}</p>
              </div>
              <div className="grid gap-1">
                <p className="text-sm font-medium text-muted-foreground">{t("form.fields.email")}</p>
                <a
                  href={`mailto:${teacher.email}`}
                  className="text-primary underline-offset-2 hover:underline"
                >
                  {teacher.email}
                </a>
              </div>
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.school")}</p>
                  <p>{teacher.schoolName}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.employmentStatus")}</p>
                  <p>{t(`${teacher.employmentStatus}`)}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">{t("form.fields.isPartTime")}</p>
                  <p>{teacher.isPartTime ? t("table.yes") : t("table.no")}</p>
                </div>
                {teacher.usageCycle && (
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.usageCycle")}</p>
                    <p>{t(`${teacher.usageCycle}`)}</p>
                  </div>
                )}
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.school")}</p>
                    <p>{teacher.schoolName}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.employmentStatus")}</p>
                    <p>{t(`form.employmentStatus.${teacher.employmentStatus}`)}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.isPartTime")}</p>
                    <p>{teacher.isPartTime ? t("table.yes") : t("table.no")}</p>
                  </div>
                  {teacher.usageCycle && (
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">{t("form.fields.usageCycle")}</p>
                      <p>{t(`form.usageCycle.${teacher.usageCycle}`)}</p>
                    </div>
                  )}
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">{t("form.fields.isActive")}</p>
                    {teacher.isActive ? (
                      <Badge variant="success">
                        {t("status.active")}
                      </Badge>
                    ) : (
                      <Badge variant="secondary">
                        {t("status.inactive")}
                      </Badge>
                    )}
                  </div>
                </div>
              </div>
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
            onCloseStatus();
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
              {warningMessage && (
                <div className="mt-3 p-3 rounded-md bg-amber-50 border border-amber-200 text-amber-900 dark:bg-amber-500/10 dark:border-amber-400/30 dark:text-amber-100">
                  <p className="text-sm font-medium">{t("status.warning")}</p>
                  <p className="text-sm mt-1">{warningMessage}</p>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isSubmitting} onClick={onCloseStatus}>
              {t("actions.cancel")}
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={onStatusChange}
              disabled={isSubmitting || !statusTarget.teacher}
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

