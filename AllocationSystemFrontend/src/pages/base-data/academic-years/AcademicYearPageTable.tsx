import { DataTable } from "@/components/common/DataTable";
import type { ColumnConfig } from "@/types/datatable.types";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";

export function AcademicYearPageTable(props: {
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
}) {
    const {
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
    } = props;

    return (
        <DataTable
            columnConfig={columnConfig}
            data={data}
            searchKey="yearName"
            searchPlaceholder={searchPlaceholder}
            enableSearch={true}
            enableColumnVisibility={true}
            enablePagination={true}
            loading={loading}
            error={error}
            emptyMessage={emptyMessage}
            disableInternalDialog={true}
            actions={{
                onView,
                onEdit,
                onDelete,
                labels,
            }}
        />
    );
}
