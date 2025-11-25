import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue, } from "@/components/ui/select";
import { AlertCircle, Loader2 } from "lucide-react";
import type { TeacherSubject, CreateTeacherSubjectRequest, UpdateTeacherSubjectRequest } from "../types/teacherSubject.types";
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import { TeacherService } from "@/features/teachers/services/teacherService";
import { SubjectService } from "@/features/subjects/services/subjectService";
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";

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
        <div className="space-y-2 col-span-1">
          <label htmlFor="academicYearId" className="text-sm font-medium">
            {t("form.fields.academicYear")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.academicYearId > 0 ? String(formData.academicYearId) : ""}
            onValueChange={(value) => handleChange("academicYearId", Number(value))}
            disabled={isLoading || isSubmitting || loadingAcademicYears}
          >
            <SelectTrigger className={errors.academicYearId ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.academicYear")} />
            </SelectTrigger>
            <SelectContent>
              {academicYears.length === 0 && !loadingAcademicYears ? (
                <SelectItem value="" disabled>
                  {t("form.placeholders.noAcademicYears") || "No academic years available"}
                </SelectItem>
              ) : (
                academicYears.map((year) => (
                  <SelectItem key={year.id} value={String(year.id)}>
                    {year.yearName}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.academicYearId && (
            <p className="text-sm text-destructive">{errors.academicYearId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="teacherId" className="text-sm font-medium">
            {t("form.fields.teacher")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.teacherId > 0 ? String(formData.teacherId) : ""}
            onValueChange={(value) => handleChange("teacherId", Number(value))}
            disabled={isLoading || isSubmitting || loadingTeachers}
          >
            <SelectTrigger className={errors.teacherId ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.teacher")} />
            </SelectTrigger>
            <SelectContent>
              {teachers.length === 0 && !loadingTeachers ? (
                <SelectItem value="" disabled>
                  {t("form.placeholders.noTeachers")}
                </SelectItem>
              ) : (
                teachers.map((teacher) => (
                  <SelectItem key={teacher.id} value={String(teacher.id)}>
                    {teacher.firstName} {teacher.lastName} ({teacher.email})
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.teacherId && (
            <p className="text-sm text-destructive">{errors.teacherId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="subjectId" className="text-sm font-medium">
            {t("form.fields.subject")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.subjectId > 0 ? String(formData.subjectId) : ""}
            onValueChange={(value) => handleChange("subjectId", Number(value))}
            disabled={isLoading || isSubmitting || loadingSubjects}
          >
            <SelectTrigger className={errors.subjectId ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.subject")} />
            </SelectTrigger>
            <SelectContent>
              {subjects.length === 0 && !loadingSubjects ? (
                <SelectItem value="" disabled>
                  {t("form.placeholders.noSubjects")}
                </SelectItem>
              ) : (
                subjects.map((subject) => (
                  <SelectItem key={subject.id} value={String(subject.id)}>
                    {subject.subjectCode} - {subject.subjectTitle}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.subjectId && (
            <p className="text-sm text-destructive">{errors.subjectId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="availabilityStatus" className="text-sm font-medium">
            {t("form.fields.availabilityStatus")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.availabilityStatus}
            onValueChange={(value) => handleChange("availabilityStatus", value)}
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger className={errors.availabilityStatus ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.availabilityStatus")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="AVAILABLE">{t("table.available")}</SelectItem>
              <SelectItem value="NOT_AVAILABLE">{t("table.notAvailable")}</SelectItem>
              <SelectItem value="LIMITED">{t("table.limited")}</SelectItem>
              <SelectItem value="PREFERRED">{t("table.preferred")}</SelectItem>
            </SelectContent>
          </Select>
          {errors.availabilityStatus && (
            <p className="text-sm text-destructive">{errors.availabilityStatus}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="gradeLevelFrom" className="text-sm font-medium">
            {t("form.fields.gradeLevelFrom")}
          </label>
          <Input
            id="gradeLevelFrom"
            type="number"
            value={formData.gradeLevelFrom ?? ""}
            onChange={(e) =>
              handleChange("gradeLevelFrom", e.target.value === "" ? null : Number(e.target.value))
            }
            placeholder={t("form.placeholders.gradeLevelFrom")}
            disabled={isLoading || isSubmitting}
            min={0}
          />
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="gradeLevelTo" className="text-sm font-medium">
            {t("form.fields.gradeLevelTo")}
          </label>
          <Input
            id="gradeLevelTo"
            type="number"
            value={formData.gradeLevelTo ?? ""}
            onChange={(e) =>
              handleChange("gradeLevelTo", e.target.value === "" ? null : Number(e.target.value))
            }
            placeholder={t("form.placeholders.gradeLevelTo")}
            disabled={isLoading || isSubmitting}
            min={0}
          />
        </div>

        <div className="space-y-2 md:col-span-2">
          <label htmlFor="notes" className="text-sm font-medium">
            {t("form.fields.notes")}
          </label>
          <Input
            id="notes"
            value={formData.notes ?? ""}
            onChange={(e) => handleChange("notes", e.target.value)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            maxLength={1000}
          />
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isLoading || isSubmitting}
        >
          {tCommon("actions.cancel")}
        </Button>
        <Button type="submit" disabled={isLoading || isSubmitting}>
          {isSubmitting || isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              {tCommon("actions.saving")}
            </>
          ) : teacherSubject ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}