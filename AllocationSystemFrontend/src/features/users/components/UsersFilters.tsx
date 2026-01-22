import { useTranslation } from "react-i18next";
import type { AccountStatus, UserRole } from "@/features/users/types/user.types";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type EnabledFilter = "all" | "true" | "false";

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

const ALL = "__all__" as const;

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
}: UsersFiltersProps) {
  const { t } = useTranslation("users");
  const { t: tCommon } = useTranslation("common");

  const roleValue = role ?? ALL;
  const statusValue = status ?? ALL;

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
          <Label>{t("filters.role")}</Label>
          <Select
            value={roleValue}
            onValueChange={(v) =>
              onRoleChange(v === ALL ? undefined : (v as UserRole))
            }
          >
            <SelectTrigger className="h-10 w-full">
              <SelectValue placeholder={t("filters.all")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("filters.all")}</SelectItem>
              <SelectItem value="ADMIN">ADMIN</SelectItem>
              <SelectItem value="MODERATOR">MODERATOR</SelectItem>
              <SelectItem value="USER">USER</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Status */}
        <div className="space-y-2">
          <Label>{t("filters.status")}</Label>
          <Select
            value={statusValue}
            onValueChange={(v) =>
              onStatusChange(v === ALL ? undefined : (v as AccountStatus))
            }
          >
            <SelectTrigger className="h-10 w-full">
              <SelectValue placeholder={t("filters.all")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={ALL}>{t("filters.all")}</SelectItem>
              <SelectItem value="ACTIVE">ACTIVE</SelectItem>
              <SelectItem value="INACTIVE">INACTIVE</SelectItem>
              <SelectItem value="SUSPENDED">SUSPENDED</SelectItem>
              <SelectItem value="PENDING_VERIFICATION">PENDING_VERIFICATION</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Enabled */}
        <div className="space-y-2">
          <Label>{t("filters.enabled")}</Label>
          <Select
            value={enabled}
            onValueChange={(v) => onEnabledChange(v as EnabledFilter)}
          >
            <SelectTrigger className="h-10 w-full">
              <SelectValue placeholder={t("filters.all")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t("filters.all")}</SelectItem>
              <SelectItem value="true">{t("status.enabled")}</SelectItem>
              <SelectItem value="false">{t("status.disabled")}</SelectItem>
            </SelectContent>
          </Select>
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
