import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { DataTable } from "@/components/common/DataTable";
import { TeacherSubjectService } from "@/features/teacher-subjects/services/teacherSubjectService";
import {
  TeacherSubjectDialogs,
  useTeacherSubjectsColumnConfig,
} from "@/features/teacher-subjects";
import type { TeacherSubject } from "@/features/teacher-subjects/types/teacherSubject.types";
import { apiClient } from "@/lib/api-client";
import { useTranslation } from "react-i18next";
import type { Teacher } from "@/features/teachers";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import { useDialogState } from "@/hooks/useDialogState";

export default function TeacherDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation("teacherSubjects");
  const dialogs = useDialogState();
  const [teacher, setTeacher] = useState<Teacher | null>(null);
  const [subjects, setSubjects] = useState<TeacherSubject[]>([]);
  const [selectedTeacherSubject, setSelectedTeacherSubject] = useState<TeacherSubject | null>(null);
  const [teacherSubjectToDelete, setTeacherSubjectToDelete] = useState<TeacherSubject | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Fetch teacher details
  useEffect(() => {
    async function fetchTeacher() {
      setLoading(true);
      setError(null);
      try {
        const response = await apiClient.get<{ data: Teacher }>(`/teachers/${id}`);
        setTeacher(response.data);
      } catch (err) {
        console.log(err)
        setError(t("errors.teacherNotFound") || "Teacher not found");
      } finally {
        setLoading(false);
      }
    }
    if (id) fetchTeacher();
  }, [id, t]);

  // Fetch teacher subjects
  const fetchSubjects = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await TeacherSubjectService.getByTeahcerId(Number(id));
      setSubjects(data);
    } catch (err) {
        console.log(err)
      setError(t("errors.subjectsNotFound") || "Subjects not found");
    } finally {
      setLoading(false);
    }
  }, [id, t]);

  useEffect(() => {
    if (id) fetchSubjects();
  }, [id, fetchSubjects]);

  // CRUD handlers
  const handleCreate = useCallback(
    async (data: Partial<TeacherSubject>) => {
        setIsSubmitting(true);
        try {
        // Destructure and validate required fields
        const {
            academicYearId,
            subjectId,
            availabilityStatus,
            gradeLevelFrom,
            gradeLevelTo,
            notes,
        } = data;

        if (
            academicYearId === undefined ||
            subjectId === undefined ||
            availabilityStatus === undefined
        ) {
            // Optionally show an error/toast here
            setIsSubmitting(false);
            return;
        }

        await TeacherSubjectService.create({
            teacherId: Number(id),
            academicYearId,
            subjectId,
            availabilityStatus,
            gradeLevelFrom: gradeLevelFrom ?? null,
            gradeLevelTo: gradeLevelTo ?? null,
            notes: notes ?? "",
        });
        dialogs.create.setIsOpen(false);
        await fetchSubjects();
        } finally {
        setIsSubmitting(false);
        }
    },
    [id, dialogs.create, fetchSubjects]
  );

  const handleUpdate = useCallback(
    async (data: Partial<TeacherSubject>) => {
      if (!selectedTeacherSubject) return;
      setIsSubmitting(true);
      try {
        await TeacherSubjectService.update(selectedTeacherSubject.id, data);
        dialogs.edit.setIsOpen(false);
        setSelectedTeacherSubject(null);
        await fetchSubjects();
      } finally {
        setIsSubmitting(false);
      }
    },
    [selectedTeacherSubject, dialogs.edit, fetchSubjects]
  );

  const handleDelete = useCallback(async () => {
    if (!teacherSubjectToDelete) return;
    setIsSubmitting(true);
    try {
      await TeacherSubjectService.delete(teacherSubjectToDelete.id);
      dialogs.delete.setIsOpen(false);
      setTeacherSubjectToDelete(null);
      await fetchSubjects();
    } finally {
      setIsSubmitting(false);
    }
  }, [teacherSubjectToDelete, dialogs.delete, fetchSubjects]);

  const handleEditClick = useCallback((teacherSubject: TeacherSubject) => {
    setSelectedTeacherSubject(teacherSubject);
    dialogs.edit.setIsOpen(true);
  }, [setSelectedTeacherSubject, dialogs.edit]);

  const handleDeleteClick = useCallback((teacherSubject: TeacherSubject) => {
    setTeacherSubjectToDelete(teacherSubject);
    dialogs.delete.setIsOpen(true);
  }, [dialogs.delete]);

  const handleViewClick = useCallback((teacherSubject: TeacherSubject) => {
    setSelectedTeacherSubject(teacherSubject);
    dialogs.view.setIsOpen(true);
  }, [setSelectedTeacherSubject, dialogs.view]);

  const handleCreateClick = () => {
    dialogs.create.setIsOpen(true);
  };

  const columnConfig = useTeacherSubjectsColumnConfig();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[200px]">
        <span className="text-muted-foreground">{t("table.loading")}</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-destructive text-center py-8">{error}</div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Teacher Details */}
      {teacher && (
        <div className="rounded-md border p-4 bg-muted/50 space-y-2">
          <h2 className="text-xl font-semibold">{teacher.firstName} {teacher.lastName}</h2>
          <div className="text-sm text-muted-foreground">
            <div>{t("fields.email")}: {teacher.email}</div>
            <div>{t("fields.employmentStatus")}: {teacher.employmentStatus}</div>
            {/* Add more fields as needed */}
          </div>
        </div>
      )}

      {/* Subjects DataTable */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-lg font-semibold">{t("table.teacherSubjects") || "Teacher Subjects"}</h3>
          <Button onClick={handleCreateClick}>
            <Plus className="mr-2 h-4 w-4" />
            {t("actions.create")}
          </Button>
        </div>
        <DataTable
          columnConfig={columnConfig}
          data={subjects}
          searchKey="subjectTitle"
          searchPlaceholder={t("table.searchPlaceholder")}
          enableSearch={true}
          enableColumnVisibility={true}
          enablePagination={false}
          loading={loading}
          error={error}
          emptyMessage={t("table.emptyMessage")}
          disableInternalDialog={true}
          actions={{
            onView: handleViewClick,
            onEdit: handleEditClick,
            onDelete: handleDeleteClick,
            labels: {
              view: t("actions.view"),
              edit: t("actions.edit"),
              delete: t("actions.delete"),
            },
          }}
        />
      </div>

      <TeacherSubjectDialogs
        isCreateDialogOpen={dialogs.create.isOpen}
        setIsCreateDialogOpen={dialogs.create.setIsOpen}
        isEditDialogOpen={dialogs.edit.isOpen}
        setIsEditDialogOpen={dialogs.edit.setIsOpen}
        isViewDialogOpen={dialogs.view.isOpen}
        setIsViewDialogOpen={dialogs.view.setIsOpen}
        isDeleteDialogOpen={dialogs.delete.isOpen}
        setIsDeleteDialogOpen={dialogs.delete.setIsOpen}
        selectedTeacherSubject={selectedTeacherSubject}
        onCreateSubmit={handleCreate}
        onUpdateSubmit={handleUpdate}
        onDelete={handleDelete}
        onEditClick={handleEditClick}
        onSelectedChange={setSelectedTeacherSubject}
        isSubmitting={isSubmitting}
        t={t}
      />
    </div>
  );
}