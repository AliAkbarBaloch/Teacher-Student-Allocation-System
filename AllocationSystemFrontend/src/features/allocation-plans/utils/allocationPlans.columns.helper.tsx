import type { ReactNode } from "react";
import { Badge } from "@/components/ui/badge";
import type { ColumnConfig } from "@/types/datatable.types";

export function formatDateTime(value: unknown): string {
    if (typeof value === "string") {
        return new Date(value).toLocaleString();
    }
    return "-";
}


export function renderStatusBadge(value: unknown): ReactNode {
    const status = typeof value === "string" ? value : "";

    let variant:
        | "default"
        | "success"
        | "destructive"
        | "secondary"
        | "muted"
        | "outline"
        | null
        | undefined = "default";

    if (status === "Approved") {
        variant = "success";
    } else if (status === "In Review" || status === "Archived") {
        variant = "destructive";
    }

    return <Badge variant={variant}>{status}</Badge>;
}

export function renderIsCurrentBadge(
    t: (key: string) => string,
    value: unknown
): ReactNode {
    const isCurrent = typeof value === "boolean" ? value : false;

    if (isCurrent) {
        return <Badge variant="success">{t("table.current")}</Badge>;
    }

    return <Badge variant="default">{t("table.notCurrent")}</Badge>;
}

export function planNameColumn(t: (key: string) => string): ColumnConfig {
    return {
        field: "planName",
        title: t("table.planName"),
        enableSorting: true,
        fieldType: "text",
        fieldRequired: true,
        fieldPlaceholder: t("form.placeholders.planName"),
    };
}

export function planVersionColumn(t: (key: string) => string): ColumnConfig {
    return {
        field: "planVersion",
        title: t("table.planVersion"),
        enableSorting: true,
        fieldType: "text",
        fieldRequired: true,
        fieldPlaceholder: t("form.placeholders.planVersion"),
        width: "120px",
        maxWidth: "180px",
    };
}

export function yearNameColumn(t: (key: string) => string): ColumnConfig {
    return {
        field: "yearName",
        title: t("table.yearName"),
        enableSorting: true,
        fieldType: "text",
        fieldRequired: true,
        fieldPlaceholder: t("form.placeholders.yearName"),
        width: "160px",
        maxWidth: "200px",
    };
}

export function statusColumns(t: (key: string) => string): ColumnConfig[] {
    return [
        {
            field: "statusDisplayName",
            title: t("table.status"),
            enableSorting: true,
            fieldType: "text",
            format: renderStatusBadge,
            width: "120px",
            maxWidth: "160px",
        },
        {
            field: "isCurrent",
            title: t("table.isCurrent"),
            enableSorting: true,
            fieldType: "text",
            format: (value: unknown) => renderIsCurrentBadge(t, value),
            width: "100px",
            maxWidth: "140px",
        },
    ];
}

export function auditColumns(t: (key: string) => string): ColumnConfig[] {
    return [
        {
            field: "createdAt",
            title: t("table.createdAt"),
            enableSorting: true,
            fieldType: "date",
            format: formatDateTime,
            width: "160px",
            maxWidth: "200px",
        },
        {
            field: "updatedAt",
            title: t("table.updatedAt"),
            enableSorting: true,
            fieldType: "date",
            format: formatDateTime,
            width: "160px",
            maxWidth: "200px",
        },
    ];
}
