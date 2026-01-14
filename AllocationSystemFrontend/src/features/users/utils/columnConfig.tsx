import { useTranslation } from "react-i18next";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";
import type { User } from "@/features/users/types/user.types";

//a function that returns an array of table columns 
export function useUsersColumnConfig(): ColumnConfig[] {

    //
    const { t } = useTranslation("users");

    return [
        //first element in an array 
        {
            //column 1 - fullName 

            field: "fullName",
            title: t("table.columns.fullName"),
            enableSorting: true,

            format: (value: unknown, row?: unknown) => {

                const user = row as User;

                return (
                    <div className="min-w-0">
                        //value = fullName 
                        <p className="font-medium truncate" title={String(value || "")}>
                            {String(value || "")}
                        </p>

                        {/* small secondary line like Schools.address */}
                        {user.phoneNumber && (
                            <p
                                className="text-xs text-muted-foreground truncate"
                                title={user.phoneNumber}
                            >
                                {user.phoneNumber}
                            </p>
                        )}
                    </div>
                );
            },
        },
        //column 2 - email 
        {
            field: "email",
            title: t("table.columns.email"),
            enableSorting: true,
            format: (value: unknown) => {
                const email = String(value || "");
                if (!email) return <span className="text-muted-foreground">—</span>;

                return (
                    <a
                        href={`mailto:${email}`}
                        className="text-primary underline-offset-2 hover:underline truncate block"
                        title={email}
                    >
                        {email}
                    </a>
                );
            },
        },
        //column 3 - role 
        {
            field: "role",
            title: t("table.columns.role"),
            enableSorting: true,
            format: (value: unknown) => {
                return (
                    <Badge variant="outline" className="uppercase text-xs">
                        {String(value || "")}
                    </Badge>
                );
            },
        },
        //column 4 - enabled 
        {
            field: "enabled",
            title: t("table.columns.enabled"),
            enableSorting: true,
            format: (value: unknown) => {
                const enabled = typeof value === "boolean" ? value : false;
                return (
                    <Badge variant={enabled ? "success" : "secondary"}>
                        {enabled ? t("status.enabled") : t("status.disabled")}
                    </Badge>
                );
            },
        },
        //column 5 - isActive 
        {
            field: "isActive",
            title: t("table.columns.isActive"),
            enableSorting: true,
            format: (_: unknown, row?: unknown) => {
                // isActive is optional in your type, so be safe:
                const user = row as User;
                const active = typeof user.isActive === "boolean" ? user.isActive : true;

                return (
                    <Badge variant={active ? "success" : "secondary"}>
                        {active ? t("status.active") : t("status.inactive")}
                    </Badge>
                );
            },
        },
        //column accountStatus 
        {
            field: "accountStatus",
            title: t("table.columns.accountStatus"),
            enableSorting: true,
            format: (value: unknown) => {
                const status = String(value || "");
                if (!status) return <span className="text-muted-foreground">—</span>;

                return (
                    <Badge variant="outline" className="uppercase text-xs">
                        {status}
                    </Badge>
                );
            },
        },
    ];
}
