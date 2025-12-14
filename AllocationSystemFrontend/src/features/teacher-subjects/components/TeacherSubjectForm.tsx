import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { NumberField } from "@/components/form/fields/NumberField";
import { SelectField } from "@/components/form/fields/SelectField";
import { TextAreaField } from "@/components/form/fields/TextAreaField";
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import { SubjectService } from "@/features/subjects/services/subjectService";
import type { Subject } from "@/features/subjects/types/subject.types";
import { TeacherService } from "@/features/teachers/services/teacherService";
import type { Teacher } from "@/features/teachers/types/teacher.types";
import { AlertCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { CreateTeacherSubjectRequest, TeacherSubject, UpdateTeacherSubjectRequest } from "../types/teacherSubject.types";

interface TeacherSubjectFormProps {
  teacherSubject?: TeacherSubject | null;
  onSubmit: (data: CreateTeacherSubjectRequest | UpdateTeacherSubjectRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function TeacherSubjectForm({
  teacherSubject,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: TeacherSubjectFormProps) {
  const { t } = useTranslation("teacherSubjects");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreateTeacherSubjectRequest>(() => {
    if (teacherSubject) {
      return {
        academicYearId: teacherSubject.academicYearId,
        teacherId: teacherSubject.teacherId,
        subjectId: teacherSubject.subjectId,
        availabilityStatus: teacherSubject.availabilityStatus,
        gradeLevelFrom: teacherSubject.gradeLevelFrom ?? null,
        gradeLevelTo: teacherSubject.gradeLevelTo ?? null,
        notes: teacherSubject.notes ?? "",
      };
    }
    return {
      academicYearId: 0,
      teacherId: 0,
      subjectId: 0,
      availabilityStatus: "AVAILABLE",
      gradeLevelFrom: null,
      gradeLevelTo: null,
      notes: "",
    };
  });

  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loadingAcademicYears, setLoadingAcademicYears] = useState(true);
  const [loadingTeachers, setLoadingTeachers] = useState(true);
  const [loadingSubjects, setLoadingSubjects] = useState(true);
  const [errors, setErrors] = useState<Partial<Record<keyof CreateTeacherSubjectRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const loadAcademicYears = async () => {
      try {
        const data = await AcademicYearService.getAll();
        setAcademicYears(data ?? []);
      } catch (err) {
        console.log(err);
        setAcademicYears([]);
      } finally {
        setLoadingAcademicYears(false);
      }
    };
    loadAcademicYears();
  }, []);

  useEffect(() => {
    const loadTeachers = async () => {
      try {
        const data = await TeacherService.list({ pageSize: 100 });
        setTeachers(data.items ?? []);
      } catch (err) {
        console.log(err);
        setTeachers([]);
      } finally {
        setLoadingTeachers(false);
      }
    };
    loadTeachers();
  }, []);

  useEffect(() => {
    const loadSubjects = async () => {
      try {
        const data = await SubjectService.getAll();
        setSubjects(data ?? []);
      } catch (err) {
        console.log(err);
        setSubjects([]);
      } finally {
        setLoadingSubjects(false);
      }
    };
    loadSubjects();
  }, []);

  useEffect(() => {
    if (teacherSubject) {
      setFormData({
        academicYearId: teacherSubject.academicYearId,
        teacherId: teacherSubject.teacherId,
        subjectId: teacherSubject.subjectId,
        availabilityStatus: teacherSubject.availabilityStatus,
        gradeLevelFrom: teacherSubject.gradeLevelFrom ?? null,
        gradeLevelTo: teacherSubject.gradeLevelTo ?? null,
        notes: teacherSubject.notes ?? "",
      });
    } else {
      setFormData({
        academicYearId: 0,
        teacherId: 0,
        subjectId: 0,
        availabilityStatus: "AVAILABLE",
        gradeLevelFrom: null,
        gradeLevelTo: null,
        notes: "",
      });
    }
    setErrors({});
  }, [teacherSubject]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateTeacherSubjectRequest, string>> = {};

    if (!formData.academicYearId || formData.academicYearId <= 0) {
      newErrors.academicYearId = t("form.errors.academicYearRequired");
    }
    if (!formData.teacherId || formData.teacherId <= 0) {
      newErrors.teacherId = t("form.errors.teacherRequired");
    }
    if (!formData.subjectId || formData.subjectId <= 0) {
      newErrors.subjectId = t("form.errors.subjectRequired");
    }
    if (!formData.availabilityStatus || typeof formData.availabilityStatus !== "string") {
      newErrors.availabilityStatus = t("form.errors.availabilityStatusRequired");
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      if (teacherSubject) {
        const updateData: UpdateTeacherSubjectRequest = {
          availabilityStatus: formData.availabilityStatus,
          gradeLevelFrom: formData.gradeLevelFrom,
          gradeLevelTo: formData.gradeLevelTo,
          notes: formData.notes,
        };
        await onSubmit(updateData);
      } else {
        const createData: CreateTeacherSubjectRequest = {
          academicYearId: formData.academicYearId,
          teacherId: formData.teacherId,
          subjectId: formData.subjectId,
          availabilityStatus: formData.availabilityStatus,
          gradeLevelFrom: formData.gradeLevelFrom,
          gradeLevelTo: formData.gradeLevelTo,
          notes: formData.notes,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateTeacherSubjectRequest, value: string | number | null) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 py-4">
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <AlertCircle className="h-4 w-4" />
          <span>{externalError || Object.values(errors)[0]}</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <SelectField
          id="academicYearId"
          label={t("form.fields.academicYear")}
          value={formData.academicYearId > 0 ? String(formData.academicYearId) : ""}
          onChange={value => handleChange("academicYearId", Number(value))}
          options={
            academicYears.length === 0 && !loadingAcademicYears
              ? [
                  {
                    value: "",
                    label: t("form.placeholders.noAcademicYears") || "No academic years available",
                    disabled: true,
                  },
                ]
              : academicYears.map(year => ({
                  value: String(year.id),
                  label: year.yearName,
                }))
          }
          placeholder={t("form.placeholders.academicYear")}
          required
          error={errors.academicYearId}
          disabled={isLoading || isSubmitting || loadingAcademicYears}
        />

        <SelectField
          id="teacherId"
          label={t("form.fields.teacher")}
          value={formData.teacherId > 0 ? String(formData.teacherId) : ""}
          onChange={value => handleChange("teacherId", Number(value))}
          options={
            teachers.length === 0 && !loadingTeachers
              ? [
                  {
                    value: "",
                    label: t("form.placeholders.noTeachers") || "No teachers available",
                    disabled: true,
                  },
                ]
              : teachers.map(teacher => ({
                  value: String(teacher.id),
                  label: `${teacher.firstName} ${teacher.lastName} (${teacher.email})`,
                }))
          }
          placeholder={t("form.placeholders.teacher")}
          required
          error={errors.teacherId}
          disabled={isLoading || isSubmitting || loadingTeachers}
        />

        <SelectField
          id="subjectId"
          label={t("form.fields.subject")}
          value={formData.subjectId > 0 ? String(formData.subjectId) : ""}
          onChange={value => handleChange("subjectId", Number(value))}
          options={
            subjects.length === 0 && !loadingSubjects
              ? [
                  {
                    value: "",
                    label: t("form.placeholders.noSubjects") || "No subjects available",
                    disabled: true,
                  },
                ]
              : subjects.map(subject => ({
                  value: String(subject.id),
                  label: `${subject.subjectCode} - ${subject.subjectTitle}`,
                }))
          }
          placeholder={t("form.placeholders.subject")}
          required
          error={errors.subjectId}
          disabled={isLoading || isSubmitting || loadingSubjects}
        />

        <SelectField
          id="availabilityStatus"
          label={t("form.fields.availabilityStatus")}
          value={formData.availabilityStatus}
          onChange={value => handleChange("availabilityStatus", value)}
          options={[
            { value: "AVAILABLE", label: t("table.available") },
            { value: "NOT_AVAILABLE", label: t("table.notAvailable") },
            { value: "LIMITED", label: t("table.limited") },
            { value: "PREFERRED", label: t("table.preferred") },
          ]}
          placeholder={t("form.placeholders.availabilityStatus")}
          required
          error={errors.availabilityStatus}
          disabled={isLoading || isSubmitting}
        />

        <NumberField
          id="gradeLevelFrom"
          label={t("form.fields.gradeLevelFrom")}
          value={formData.gradeLevelFrom ?? ""}
          onChange={value => handleChange("gradeLevelFrom", value)}
          placeholder={t("form.placeholders.gradeLevelFrom")}
          disabled={isLoading || isSubmitting}
          min={0}
        />

        <NumberField
          id="gradeLevelTo"
          label={t("form.fields.gradeLevelTo")}
          value={formData.gradeLevelTo ?? ""}
          onChange={value => handleChange("gradeLevelTo", value)}
          placeholder={t("form.placeholders.gradeLevelTo")}
          disabled={isLoading || isSubmitting}
          min={0}
        />

        <TextAreaField
          id="notes"
          label={t("form.fields.notes")}
          value={formData.notes ?? ""}
          onChange={value => handleChange("notes", value)}
          placeholder={t("form.placeholders.notes")}
          disabled={isLoading || isSubmitting}
          rows={3}
          className="md:col-span-2"
        />
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <CancelButton
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </CancelButton>
        <SubmitButton
          isLoading={isSubmitting || isLoading}
          isEdit={!!teacherSubject}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}