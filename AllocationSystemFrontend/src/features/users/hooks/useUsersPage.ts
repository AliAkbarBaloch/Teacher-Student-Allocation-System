import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";

//API helper 
import { UserService } from "@/features/users/services/userService";
import type {
    User,
    UserRole,
    AccountStatus,
    CreateUserRequest,
    UpdateUserRequest,
    UsersListParams,
    PasswordResetRequest,
} from "@/features/users/types/user.types";

import { usePagination } from "@/hooks/usePagination";
import { useDebounce } from "@/hooks/useDebounce";
import { DEFAULT_TABLE_PAGE_SIZE } from "@/lib/constants/pagination";
import { SEARCH_DEBOUNCE_MS } from "@/lib/constants/app";

/*
Filters state stored in the UI.
 */
type UsersFiltersState = {
    search?: string;
    role?: UserRole;
    status?: AccountStatus;
    enabled: "all" | "true" | "false";
};

//user hook function 
export function useUsersPage() {

    const { t } = useTranslation("users");

    // List of users shown in the table. starts as empty array 
    const [users, setUsers] = useState<User[]>([]);
    //
    const [selectedUser, setSelectedUser] = useState<User | null>(null);

    
    // UI state
    //
    const [loading, setLoading] = useState(false);
    //loading for edit dialog 
    const [formLoading, setFormLoading] = useState(false);
    //stroing error messages 
    const [error, setError] = useState<string | null>(null);

    // true when you are creating/updating/deleting/activating/reseting operations
    const [isSubmitting, setIsSubmitting] = useState(false);

    // For showing spinner next to search input like in Schools
    const [isSearchInputLoading, setIsSearchInputLoading] = useState(false);

    
    // Dialog targets (for confirmation dialogs)
    const [statusTarget, setStatusTarget] = useState<{
        user: User | null;
        nextState: boolean; // true=activate, false=deactivate
    }>({ user: null, nextState: true });

    //stores which user is about to be deleted 
    const [deleteTarget, setDeleteTarget] = useState<User | null>(null);

    //stores which user is about to have password reset 
    const [resetTarget, setResetTarget] = useState<User | null>(null);

    // -----------------------------
    // Pagination
    // pagination.page is usually 1-based in UI
    // backend users endpoint expects 0-based page index
    // -----------------------------
    const { pagination, handlePageChange, handlePageSizeChange, updatePagination } =
        usePagination(DEFAULT_TABLE_PAGE_SIZE);

    // -----------------------------
    // Filters (controlled inputs)
    // -----------------------------
    // what the user types in the search box 
    const [searchInput, setSearchInput] = useState("");
    // update debouncedSearch when user stops typing 
    const debouncedSearch = useDebounce(searchInput, SEARCH_DEBOUNCE_MS);

    // Stores filter dropdown values 
    const [roleFilter, setRoleFilter] = useState<UserRole | undefined>(undefined);
    const [statusFilter, setStatusFilter] = useState<AccountStatus | undefined>(undefined);
    const [enabledFilter, setEnabledFilter] = useState<UsersFiltersState["enabled"]>("all");

    // Create one single filter object, but only rebuilds it when any dependency changes 
    const filters = useMemo<UsersFiltersState>(() => {
        return {
            search: debouncedSearch || undefined,
            role: roleFilter,
            status: statusFilter,
            enabled: enabledFilter,
        };
    }, [debouncedSearch, roleFilter, statusFilter, enabledFilter]);

    // Convert filters to backend query params
    const apiParams = useMemo<UsersListParams>(() => {
        const enabledValue =
            filters.enabled === "all"
                ? undefined
                : filters.enabled === "true"
                    ? true
                    : false;

        return {
            search: filters.search,
            role: filters.role,
            status: filters.status,
            enabled: enabledValue,
            // backend uses 0-based page index:
            page: Math.max(0, (pagination.page ?? 1) - 1),
            size: pagination.pageSize,
            sortBy: "createdAt",
            sortDirection: "DESC",
        };
    }, [filters, pagination.page, pagination.pageSize]);

    // Load users
    const loadUsers = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const page = await UserService.getPaginated(apiParams);

            setUsers(page.content ?? []);

            // Convert backend pagination -> UI pagination
            updatePagination({
                // backend page.number is 0-based, UI expects 1-based
                page: (page.number ?? 0) + 1,
                pageSize: page.size ?? pagination.pageSize,
                totalItems: page.totalElements ?? 0,
                totalPages: page.totalPages ?? 0,
            });
        } catch (err) {
            const message = err instanceof Error ? err.message : t("errors.load");
            setError(message);
        } finally {
            setLoading(false);
            setIsSearchInputLoading(false);
        }
    }, [apiParams, pagination.pageSize, t, updatePagination]);

    useEffect(() => {
        loadUsers();
    }, [loadUsers]);

    const refreshList = useCallback(async () => {
        await loadUsers();
    }, [loadUsers]);

    const resetToFirstPage = useCallback(() => {
        handlePageChange(1);
    }, [handlePageChange]);

    // -----------------------------
    // Filter handlers
    // -----------------------------
    const handleSearchChange = useCallback(
        (value: string) => {
            setSearchInput(value);
            resetToFirstPage();
            setIsSearchInputLoading(true);
        },
        [resetToFirstPage]
    );

    const handleRoleFilterChange = useCallback(
        (value?: UserRole) => {
            setRoleFilter(value);
            resetToFirstPage();
        },
        [resetToFirstPage]
    );

    const handleStatusFilterChange = useCallback(
        (value?: AccountStatus) => {
            setStatusFilter(value);
            resetToFirstPage();
        },
        [resetToFirstPage]
    );

    const handleEnabledFilterChange = useCallback(
        (value: UsersFiltersState["enabled"]) => {
            setEnabledFilter(value);
            resetToFirstPage();
        },
        [resetToFirstPage]
    );

    const handleResetFilters = useCallback(() => {
        setSearchInput("");
        setRoleFilter(undefined);
        setStatusFilter(undefined);
        setEnabledFilter("all");
        resetToFirstPage();
        setIsSearchInputLoading(false);
    }, [resetToFirstPage]);

    // -----------------------------
    // Fetch user details (for edit/view dialog)
    // -----------------------------
    const fetchUserDetails = useCallback(
        async (id: number) => {
            setFormLoading(true);
            try {
                const user = await UserService.getById(id);
                setSelectedUser(user);
                return user;
            } catch (err) {
                const message = err instanceof Error ? err.message : t("errors.load");
                toast.error(message);
                throw err;
            } finally {
                setFormLoading(false);
            }
        },
        [t]
    );

    // Create user (optimistic)

    const handleCreateSubmit = useCallback(
        async (payload: CreateUserRequest) => {
            setIsSubmitting(true);
            let tempId: number | null = null;

            try {
                tempId = -Date.now();

                const optimisticUser: User = {
                    id: tempId,
                    email: payload.email,
                    fullName: payload.fullName,
                    role: payload.role,
                    roleId: payload.roleId ?? 0,
                    phoneNumber: payload.phoneNumber,
                    enabled: payload.enabled ?? true,
                    isActive: payload.isActive ?? true,
                    accountLocked: false,
                    accountStatus: payload.accountStatus ?? "ACTIVE",
                    failedLoginAttempts: 0,
                    loginAttempt: 0,
                    createdAt: new Date().toISOString(),
                    updatedAt: new Date().toISOString(),
                };

                setUsers((prev) => [optimisticUser, ...prev]);

                const created = await UserService.create(payload);

                setUsers((prev) => prev.map((u) => (u.id === tempId ? created : u)));
                toast.success(t("notifications.createSuccess"));

                await refreshList();
                return created;
            } catch (err) {
                if (tempId !== null) {
                    setUsers((prev) => prev.filter((u) => u.id !== tempId));
                }
                const message = err instanceof Error ? err.message : t("errors.submit");
                toast.error(message);
                throw err;
            } finally {
                setIsSubmitting(false);
            }
        },
        [refreshList, t]
    );

    // -----------------------------
    // Update user (optimistic)
    // -----------------------------
    const handleUpdateSubmit = useCallback(
        async (payload: UpdateUserRequest) => {
            if (!selectedUser) return;

            setIsSubmitting(true);
            const previous = selectedUser;

            try {
                const optimistic: User = { ...selectedUser, ...payload };
                setUsers((prev) => prev.map((u) => (u.id === selectedUser.id ? optimistic : u)));
                setSelectedUser(optimistic);

                const updated = await UserService.update(selectedUser.id, payload);

                setUsers((prev) => prev.map((u) => (u.id === selectedUser.id ? updated : u)));
                setSelectedUser(updated);

                toast.success(t("notifications.updateSuccess"));
                await refreshList();

                return updated;
            } catch (err) {
                // rollback
                setUsers((prev) => prev.map((u) => (u.id === previous.id ? previous : u)));
                setSelectedUser(previous);

                const message = err instanceof Error ? err.message : t("errors.submit");
                toast.error(message);
                throw err;
            } finally {
                setIsSubmitting(false);
            }
        },
        [selectedUser, refreshList, t]
    );

    // -----------------------------
    // Activate / Deactivate (optimistic)
    // -----------------------------
    const handleStatusChange = useCallback(
        async (user: User, nextState: boolean) => {
            setStatusTarget({ user, nextState });
            setIsSubmitting(true);

            try {
                // optimistic update on list
                setUsers((prev) =>
                    prev.map((u) => (u.id === user.id ? { ...u, isActive: nextState } : u))
                );

                const updated = nextState
                    ? await UserService.activate(user.id)
                    : await UserService.deactivate(user.id);

                setUsers((prev) => prev.map((u) => (u.id === user.id ? updated : u)));

                toast.success(
                    nextState ? t("notifications.activateSuccess") : t("notifications.deactivateSuccess")
                );

                setStatusTarget({ user: null, nextState: true });
                await refreshList();

                return updated;
            } catch (err) {
                // rollback
                setUsers((prev) => prev.map((u) => (u.id === user.id ? user : u)));

                const message = err instanceof Error ? err.message : t("errors.submit");
                toast.error(message);
                throw err;
            } finally {
                setIsSubmitting(false);
            }
        },
        [refreshList, t]
    );

    // -----------------------------
    // Reset password (no optimistic list change)
    // -----------------------------
    const handleResetPassword = useCallback(
        async (user: User, payload: PasswordResetRequest) => {
            setResetTarget(user);
            setIsSubmitting(true);

            try {
                const updated = await UserService.resetPassword(user.id, payload);
                toast.success(t("notifications.passwordResetSuccess"));
                setResetTarget(null);
                await refreshList();
                return updated;
            } catch (err) {
                const message = err instanceof Error ? err.message : t("errors.submit");
                toast.error(message);
                throw err;
            } finally {
                setIsSubmitting(false);
            }
        },
        [refreshList, t]
    );

    // -----------------------------
    // Delete user
    // -----------------------------
    const handleDelete = useCallback(
        async (user: User) => {
            setDeleteTarget(user);
            setIsSubmitting(true);

            try {
                await UserService.delete(user.id);
                toast.success(t("notifications.deleteSuccess"));
                setDeleteTarget(null);
                await refreshList();
            } catch (err) {
                const message = err instanceof Error ? err.message : t("errors.submit");
                toast.error(message);
                throw err;
            } finally {
                setIsSubmitting(false);
            }
        },
        [refreshList, t]
    );

    return {
        // Data
        users,
        selectedUser,
        setSelectedUser,

        // Loading/Error
        loading,
        error,
        formLoading,
        isSubmitting,
        isSearchInputLoading,

        // Pagination
        pagination,
        handlePageChange,
        handlePageSizeChange,

        // Filters
        searchInput,
        roleFilter,
        statusFilter,
        enabledFilter,
        handleSearchChange,
        handleRoleFilterChange,
        handleStatusFilterChange,
        handleEnabledFilterChange,
        handleResetFilters,

        // Actions
        fetchUserDetails,
        handleCreateSubmit,
        handleUpdateSubmit,
        handleStatusChange,
        handleResetPassword,
        handleDelete,
        refreshList,

        // Dialog targets
        statusTarget,
        setStatusTarget,
        deleteTarget,
        setDeleteTarget,
        resetTarget,
        setResetTarget,
    };
}
