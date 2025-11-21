// components
import { DataTable } from "@/components/common/DataTable";
import { useTeachersColumnConfig } from "../utils/columnConfig";
// types
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { TFunction } from "i18next";
// icons
import { Power } from "lucide-react";

interface TeachersTableSectionProps {
  teachers: Teacher[];
  loading: boolean;
  isAdmin: boolean;
  t: TFunction<"teachers">;
  onViewTeacher: (teacher: Teacher) => void;
  onEditTeacher: (teacher: Teacher) => void;
  onToggleStatus: (teacher: Teacher) => void;
  onDeleteTeacher: (teacher: Teacher) => void;
}

export function TeachersTableSection({
  teachers = [],
  loading,
  isAdmin,
  t,
  onViewTeacher,
  onEditTeacher,
  onToggleStatus,
  onDeleteTeacher,
}: TeachersTableSectionProps) {
  const columnConfig = useTeachersColumnConfig();

  return (
    <DataTable
      columnConfig={columnConfig}
      data={teachers}
      enableSearch={false}
      enableColumnVisibility={true}
      enablePagination={false}
      loading={loading}
      error={null}
      emptyMessage={t("table.empty")}
      disableInternalDialog={true}
      actions={{
        onView: onViewTeacher,
        onEdit: isAdmin ? onEditTeacher : undefined,
        onDelete: isAdmin ? onDeleteTeacher : undefined,
        customActions: isAdmin
          ? [
              {
                label: (teacher: Teacher) =>
                  teacher.isActive ? t("actions.deactivate") : t("actions.activate"),
                icon: <Power className="h-4 w-4" />,
                onClick: onToggleStatus,
              },
            ]
          : undefined,
        labels: {
          view: t("actions.view"),
          edit: t("actions.edit"),
          delete: t("actions.delete"),
        },
      }}
    />
  );
}
