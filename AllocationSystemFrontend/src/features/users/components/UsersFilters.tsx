import { useTranslation } from "react-i18next";
import type { AccountStatus, UserRole } from "@/features/users/types/user.types";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

type EnabledFilter = "all" | "true" | "false";

//data + functions passed from parent 
interface UsersFiltersProps {
  searchValue: string;
  onSearchChange: (value: string) => void;

  role?: UserRole;
  onRoleChange: (value?: UserRole) => void;

  status?: AccountStatus;
  onStatusChange: (value?: AccountStatus) => void;

  enabled: EnabledFilter;
  onEnabledChange: (value: EnabledFilter) => void;

  onReset: () => void;
}

//the component 
export function UsersFilters({
  searchValue,
  onSearchChange,
  role,
  onRoleChange,
  status,
  onStatusChange,
  enabled,
  onEnabledChange,
  onReset,
  //props 
}: UsersFiltersProps) {
  //translations  
  const { t } = useTranslation("users");
  const { t: tCommon } = useTranslation("common");
  //UI
  return (
    <div className="rounded-lg border bg-background p-4">
      <div className="grid gap-4 md:grid-cols-4">
        {/* Search */}
        <div className="space-y-2 md:col-span-2">
          <Label htmlFor="userSearch">{t("filters.search")}</Label>
          <Input
            id="userSearch"
            value={searchValue}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder={t("filters.searchPlaceholder")}
          />
        </div>

        {/* Role */}
        <div className="space-y-2">
          <Label htmlFor="roleFilter">{t("filters.role")}</Label>
          <select
            id="roleFilter"
            className="h-10 w-full rounded-md border bg-background px-3 text-sm"
            value={role ?? ""}
            onChange={(e) => onRoleChange((e.target.value || undefined) as UserRole | undefined)}
          >
            <option value="">{t("filters.all")}</option>
            <option value="ADMIN">ADMIN</option>
            <option value="MODERATOR">MODERATOR</option>
            <option value="USER">USER</option>
          </select>
        </div>

        {/* Status */}
        <div className="space-y-2">
          <Label htmlFor="statusFilter">{t("filters.status")}</Label>
          <select
            id="statusFilter"
            className="h-10 w-full rounded-md border bg-background px-3 text-sm"
            value={status ?? ""}
            onChange={(e) =>
              onStatusChange((e.target.value || undefined) as AccountStatus | undefined)
            }
          >
            <option value="">{t("filters.all")}</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
            <option value="SUSPENDED">SUSPENDED</option>
            <option value="PENDING_VERIFICATION">PENDING_VERIFICATION</option>
          </select>
        </div>

        {/* Enabled */}
        <div className="space-y-2">
          <Label htmlFor="enabledFilter">{t("filters.enabled")}</Label>
          <select
            id="enabledFilter"
            className="h-10 w-full rounded-md border bg-background px-3 text-sm"
            value={enabled}
            onChange={(e) => onEnabledChange(e.target.value as EnabledFilter)}
          >
            <option value="all">{t("filters.all")}</option>
            <option value="true">{t("status.enabled")}</option>
            <option value="false">{t("status.disabled")}</option>
          </select>
        </div>
      </div>

      <div className="mt-4 flex justify-end">
        <Button variant="outline" onClick={onReset}>
          {tCommon("actions.reset")}
        </Button>
      </div>
    </div>
  );
}
