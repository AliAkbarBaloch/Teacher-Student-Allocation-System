import { useCallback, useState } from "react";
import { Button } from "@/components/ui/button";

import { useUsersPage } from "@/features/users/hooks/useUsersPage";
import { UsersFilters } from "@/features/users/components/UsersFilters";
import { DataTable } from "@/components/common/DataTable";
import { useUsersColumnConfig } from "@/features/users/utils/columnConfig";
import { UserDialogs } from "@/features/users/components/UserDialogs";
import type { User } from "@/features/users/types/user.types";



export default function UsersPage() {
    const {
        users,
        loading,
        error,
        searchInput,
        roleFilter,
        statusFilter,
        enabledFilter,
        handleSearchChange,
        handleRoleFilterChange,
        handleStatusFilterChange,
        handleEnabledFilterChange,
        handleResetFilters,

        selectedUser,
        setSelectedUser,
        formLoading,
        isSubmitting,

        statusTarget,
        setStatusTarget,
        deleteTarget,
        setDeleteTarget,
        resetTarget,
        setResetTarget,

        fetchUserDetails,
        handleCreateSubmit,
        handleUpdateSubmit,
        handleStatusChange,
        handleResetPassword,
        handleDelete,

        pagination,
        handlePageChange,
        handlePageSizeChange,
        roles,
    } = useUsersPage();

    // dialog flags like Schools example
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isViewOpen, setIsViewOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);
    const [isStatusOpen, setIsStatusOpen] = useState(false);
    const [isResetOpen, setIsResetOpen] = useState(false);

    const openCreate = () => {
        setSelectedUser(null);
        setIsCreateOpen(true);
    };

    const openView = (user: User) => {
        setSelectedUser(user);
        setIsViewOpen(true);
    };

    const openEdit = useCallback(
        async (user: User) => {
            try {
                await fetchUserDetails(user.id);
                setIsEditOpen(true);
            } catch {
                // toast already handled
            }
        },
        [fetchUserDetails]
    );

    const openDelete = (user: User) => {
        setDeleteTarget(user);
        setSelectedUser(user);
        setIsDeleteOpen(true);
    };

    const openToggleActive = (user: User) => {
        const current = typeof user.isActive === "boolean" ? user.isActive : user.enabled;
        setStatusTarget({ user, nextState: !current });
        setIsStatusOpen(true);
    };

    const openResetPassword = (user: User) => {
        setResetTarget(user);
        setIsResetOpen(true);
    };

    const confirmStatusChange = async () => {
        if (!statusTarget.user) return;
        await handleStatusChange(statusTarget.user, statusTarget.nextState);
        setIsStatusOpen(false);
    };

    const confirmDelete = async () => {
        if (!deleteTarget) return;
        await handleDelete(deleteTarget);
        setIsDeleteOpen(false);
    };

    const columnConfig = useUsersColumnConfig();

    const actions = {
        onView: (row: User) => openView(row),
        onEdit: (row: User) => openEdit(row),
        onDelete: (row: User) => openDelete(row),

        customActions: [
            {
                label: (u: User) => (u.isActive ? "Deactivate" : "Activate"),
                onClick: (u: User) => openToggleActive(u),
                separator: true,
            },
            {
                label: "Reset password",
                onClick: (u: User) => openResetPassword(u),
            },
        ],
    };


    return (
        <div className="space-y-6 w-full min-w-0 max-w-full">
            {/* Header */}
            <div className="flex items-center justify-between gap-3">
                <div>
                    <h1 className="text-xl font-semibold">User Management</h1>
                    <p className="text-sm text-muted-foreground">
                        Manage users, roles, and activation.
                    </p>
                </div>

                <Button onClick={openCreate}>Create user</Button>
            </div>

            {/* Filters */}
            <UsersFilters
                searchValue={searchInput}
                onSearchChange={handleSearchChange}
                role={roleFilter}
                onRoleChange={handleRoleFilterChange}
                status={statusFilter}
                onStatusChange={handleStatusFilterChange}
                enabled={enabledFilter ?? "all"}
                onEnabledChange={handleEnabledFilterChange}
                onReset={handleResetFilters}

            />

            {/* Table */}

            <DataTable<User>
                data={users}
                columnConfig={columnConfig}
                loading={loading}
                error={error}
                searchKey="email"               // optional: toolbar search uses this field
                searchPlaceholder="Search users..."
                enableRowClick={true}
                onRowClick={(row) => openView(row)}
                enablePagination={false}    // because there is server pagination below
                actions={actions}
                actionsHeader="Actions"
                disableInternalDialog={true} //the is custom UserDialog 
            />

            {/* Simple pagination (since no DataTable) */}
            <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
                <div className="text-muted-foreground">
                    Page {pagination.page} / {pagination.totalPages} • Total {pagination.totalItems}
                </div>

                <div className="flex items-center gap-2">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(Math.max(1, pagination.page - 1))}
                        disabled={pagination.page <= 1}
                    >
                        Prev
                    </Button>

                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handlePageChange(pagination.page + 1)}
                        disabled={pagination.page >= pagination.totalPages}
                    >
                        Next
                    </Button>

                    <select
                        className="h-9 rounded-md border bg-background px-2 text-sm"
                        value={pagination.pageSize}
                        onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                    >
                        {[10, 20, 50].map((s) => (
                            <option key={s} value={s}>
                                {s}/page
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Dialogs */}
            <UserDialogs
                isCreateDialogOpen={isCreateOpen}
                setIsCreateDialogOpen={setIsCreateOpen}
                isEditDialogOpen={isEditOpen}
                setIsEditDialogOpen={setIsEditOpen}
                isViewDialogOpen={isViewOpen}
                setIsViewDialogOpen={setIsViewOpen}
                isStatusDialogOpen={isStatusOpen}
                setIsStatusDialogOpen={setIsStatusOpen}
                isDeleteDialogOpen={isDeleteOpen}
                setIsDeleteDialogOpen={setIsDeleteOpen}
                isResetPasswordDialogOpen={isResetOpen}
                setIsResetPasswordDialogOpen={setIsResetOpen}
                selectedUser={selectedUser}
                formLoading={formLoading}
                statusTarget={statusTarget}
                deleteTarget={deleteTarget}
                resetTarget={resetTarget}
                onCreateSubmit={async (payload) => {
                    await handleCreateSubmit(payload);
                }}
                onUpdateSubmit={async (payload) => {
                    await handleUpdateSubmit(payload);
                }
                }
                onStatusChange={confirmStatusChange}
                onDelete={confirmDelete}
                onResetPassword={async (user, newPassword) => {
                    await handleResetPassword(user, { newPassword });
                }}
                onOpenEdit={openEdit}
                onStatusTargetChange={setStatusTarget}
                onResetTargetChange={setResetTarget}
                isSubmitting={isSubmitting}

                roles = {roles}
            />
        </div>
    );
}
