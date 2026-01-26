import { DataTable } from "@/components/common/DataTable";
import type { ColumnConfig } from "@/types/datatable.types";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";

/**
 * Props for the AcademicYearPageTable component.
 */
interface AcademicYearPageTableProps {
    columnConfig: ColumnConfig[];
    data: AcademicYear[];
    loading: boolean;
    error: string | null;
    searchPlaceholder: string;
    emptyMessage: string;
    labels: { view: string; edit: string; delete: string };
    onView: (ay: AcademicYear) => void;
    onEdit: (ay: AcademicYear) => void;
    onDelete: (ay: AcademicYear) => void;
}


export function AcademicYearPageTable({
    columnConfig,
    data,
    loading,
    error,
    searchPlaceholder,
    emptyMessage,
    labels,
    onView,
    onEdit,
    onDelete,
}: AcademicYearPageTableProps) {
    return (
        <DataTable
            columnConfig={columnConfig}
            data={data}
            searchKey="yearName"
            searchPlaceholder={searchPlaceholder}
            enableSearch
            enableColumnVisibility
            enablePagination
            loading={loading}
            error={error}
            emptyMessage={emptyMessage}
            disableInternalDialog
            actions={{ onView, onEdit, onDelete, labels }}
        />
    );
}
