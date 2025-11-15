import { useTranslation } from "react-i18next";
import { DataTable, type ColumnConfig } from "@/components/common/DataTable";


// Example: Define your data
const data = [
  {
    id: "1",
    title: "permissionParent",
    parent: "system management",
    description: "Permission Parent access.",
    createdAt: "2025-01-01",
  },
  {
    id: "2",
    title: "permission",
    parent: "system management",
    description: "Permission access.",
    createdAt: "2025-01-01",
  },
  {
    id: "3",
    title: "role",
    parent: "system management",
    description: "Role access.",
    createdAt: "2025-01-01",
  },
  {
    id: "4",
    title: "role",
    parent: "system management",
    description: "Role permission access.",
    createdAt: "2025-01-01",
  },
  {
    id: "5",
    title: "user",
    parent: "system management",
    description: "User access.",
    createdAt: "2025-01-01",
  },
  {
    id: "6",
    title: "audit log",
    parent: "system management",
    description: "Audit log access.",
    createdAt: "2025-01-01",
  },
  {
    id: "7",
    title: "dashboard",
    parent: "dashboard",
    description: "dashboard access.",
    createdAt: "2025-01-01",
  },
  {
    id: "8",
    title: "report",
    parent: "report",
    description: "report access.",
    createdAt: "2025-01-01",
  },
];

// Columns
const columns: ColumnConfig[] = [
  {
    field: "id",
    title: "ID",
  },
  {
    field: "title",
    title: "Title",
  },
  {
    field: "parent",
    title: "Parent",
  },
  {
    field: "description",
    title: "Description",
  },
  {
    field: "createdAt",
    title: "Created At",
    format: "date",
  },
];

export default function HomePage() {
  const { t } = useTranslation("common");

  const handleEdit = () => {
    console.log("Edit permission:");
    // Add edit logic here
  };

  const handleDelete = (row: (typeof data)[0]) => {
    console.log("Delete permission:", row);
    // Add your delete logic here
  };

  const handleUpdate = async (row: (typeof data)[0]) => {
    console.log("Update permission:", row);
    // Add your update logic here
    // await updateRecord(row);
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">{t("app.title")}</h1>
      <DataTable
        columnConfig={columns}
        data={data}
        searchKey="title"
        searchPlaceholder="Filter by title..."
        enableSearch={true}
        enableColumnVisibility={true}
        enablePagination={true}
        enableRowSelection={false}
        actions={{
          onView: () => {},
          onEdit: handleEdit,
          onDelete: handleDelete,
          onUpdate: handleUpdate,
          labels: {
            view: "View",
            edit: "Edit",
            delete: "Delete",
          },
        }}
      />
    </div>
  );
}
