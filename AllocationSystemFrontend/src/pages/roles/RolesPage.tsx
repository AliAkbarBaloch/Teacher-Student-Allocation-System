import { useState, useEffect, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { Plus, ShieldAlert } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/common/DataTable";
import { RoleForm } from "@/features/roles/components/RoleForm";
import { ProtectedRoleCallout } from "@/features/roles/components/ProtectedRoleCallout";
import { RoleService } from "@/features/roles/services/roleService";
import {
  Dialog,
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
import type { Role, CreateRoleRequest, UpdateRoleRequest } from "@/features/roles/types/role.types";
import { isSystemProtectedRole } from "@/features/roles/types/role.types";
import type { ColumnConfig } from "@/types/datatable.types";

export default function RolesPage() {
  const { t } = useTranslation("roles");
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [roleToDelete, setRoleToDelete] = useState<Role | null>(null);
  const [protectedDeleteDialogOpen, setProtectedDeleteDialogOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isProtectedRole = (role?: Role | null) =>
    role?.title ? isSystemProtectedRole(role.title) : false;

  const loadRoles = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await RoleService.getAll();
      setRoles(data);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("table.emptyMessage");
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  const handleCreate = async (data: CreateRoleRequest) => {
    setIsSubmitting(true);
    try {
      await RoleService.create(data);
      setIsCreateDialogOpen(false);
      await loadRoles();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("create.error");
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdate = async (data: UpdateRoleRequest) => {
    if (!selectedRole) return;

    setIsSubmitting(true);
    try {
      await RoleService.update(selectedRole.id, data);
      setIsEditDialogOpen(false);
      setSelectedRole(null);
      await loadRoles();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("update.error");
      throw new Error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!roleToDelete) return;

    try {
      await RoleService.delete(roleToDelete.id);
      setDeleteDialogOpen(false);
      setRoleToDelete(null);
      await loadRoles();
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : t("delete.error");
      setError(errorMessage);
    }
  };

  const handleEditClick = (role: Role) => {
    if (isProtectedRole(role)) {
      setSelectedRole(role);
      setIsViewDialogOpen(true);
      return;
    }
    setSelectedRole(role);
    setIsEditDialogOpen(true);
  };

  const handleDeleteClick = (role: Role) => {
    if (isProtectedRole(role)) {
      setProtectedDeleteDialogOpen(true);
      return;
    }
    setRoleToDelete(role);
    setDeleteDialogOpen(true);
  };


  const columnConfig: ColumnConfig[] = [
    {
      field: "title",
      title: t("table.title"),
      enableSorting: true,
      fieldType: "text",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.title"),
    },
    {
      field: "description",
      title: t("table.description"),
      enableSorting: false,
      fieldType: "textarea",
      fieldRequired: true,
      fieldPlaceholder: t("form.placeholders.description"),
    },
    {
      field: "createdAt",
      title: t("table.createdAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
    },
    {
      field: "updatedAt",
      title: t("table.updatedAt"),
      format: "date",
      enableSorting: true,
      fieldType: "date",
      fieldReadOnly: true,
    },
  ];


  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-2xl font-semibold tracking-tight">{t("title")}</h2>
          <p className="text-muted-foreground text-sm mt-1">{t("subtitle")}</p>
        </div>
        <Button onClick={() => setIsCreateDialogOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          {t("actions.create")}
        </Button>
      </div>

      <DataTable
        columnConfig={columnConfig}
        data={roles}
        searchKey="title"
        searchPlaceholder={t("table.searchPlaceholder")}
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={true}
        loading={loading}
        error={error}
        emptyMessage={t("table.emptyMessage")}
        disableInternalDialog={true}
        actions={{
          onView: (row: Role) => {
            setSelectedRole(row);
            setIsViewDialogOpen(true);
          },
          onEdit: handleEditClick,
          onDelete: handleDeleteClick,
          labels: {
            view: t("actions.view"),
            edit: t("actions.edit"),
            delete: t("actions.delete"),
          },
        }}
      />

      {/* Create Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.create")}</DialogTitle>
            <DialogDescription>{t("subtitle")}</DialogDescription>
          </DialogHeader>
          <RoleForm
            onSubmit={handleCreate}
            onCancel={() => setIsCreateDialogOpen(false)}
            isLoading={isSubmitting}
          />
        </DialogContent>
      </Dialog>

      {/* View Dialog (Read-only) */}
      <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>{t("form.title.view")}</DialogTitle>
            <DialogDescription asChild>
              <div>
                {selectedRole && isProtectedRole(selectedRole) && (
                  <ProtectedRoleCallout message={t("systemProtected.warning")} />
                )}
              </div>
            </DialogDescription>
          </DialogHeader>
          {selectedRole && (
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.title")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {selectedRole.title}
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">{t("form.fields.description")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 min-h-[100px]">
                  {selectedRole.description}
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-4">
                <Button
                  variant="outline"
                  onClick={() => {
                    setIsViewDialogOpen(false);
                    setSelectedRole(null);
                  }}
                >
                  {isProtectedRole(selectedRole)
                    ? t("delete.close")
                    : t("form.actions.cancel")}
                </Button>
                {!isProtectedRole(selectedRole) && (
                  <Button
                    onClick={() => {
                      setIsViewDialogOpen(false);
                      handleEditClick(selectedRole);
                    }}
                  >
                    {t("actions.edit")}
                  </Button>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              {selectedRole && isProtectedRole(selectedRole)
                ? t("form.title.view")
                : t("form.title.edit")}
            </DialogTitle>
            <DialogDescription asChild>
              <div>
                {selectedRole && isProtectedRole(selectedRole) && (
                  <ProtectedRoleCallout message={t("systemProtected.warning")} />
                )}
              </div>
            </DialogDescription>
          </DialogHeader>
          {selectedRole && (
            <RoleForm
              role={selectedRole}
              onSubmit={handleUpdate}
              onCancel={() => {
                setIsEditDialogOpen(false);
                setSelectedRole(null);
              }}
              isLoading={isSubmitting}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t("delete.title")}</AlertDialogTitle>
            <AlertDialogDescription asChild>
              <p>{t("delete.message")}</p>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeleteDialogOpen(false)}>
              {t("delete.cancel")}
            </AlertDialogCancel>
            {roleToDelete && !isProtectedRole(roleToDelete) && (
              <AlertDialogAction
                className="bg-destructive text-white hover:bg-destructive/90 focus:ring-destructive/50"
                onClick={handleDelete}
              >
                {t("delete.confirm")}
              </AlertDialogAction>
            )}
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Protected Delete Info Dialog */}
      <AlertDialog open={protectedDeleteDialogOpen} onOpenChange={setProtectedDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="flex items-center gap-2">
              <ShieldAlert className="h-5 w-5 text-yellow-600 dark:text-yellow-400" />
              {t("delete.protectedTitle")}
            </AlertDialogTitle>
            <AlertDialogDescription asChild>
              <div className="space-y-2">
                <p>{t("delete.protectedMessage")}</p>
                <p className="text-sm text-muted-foreground">{t("delete.superAdminInfo")}</p>
              </div>
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setProtectedDeleteDialogOpen(false)}>
              {t("delete.close")}
            </AlertDialogCancel>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

