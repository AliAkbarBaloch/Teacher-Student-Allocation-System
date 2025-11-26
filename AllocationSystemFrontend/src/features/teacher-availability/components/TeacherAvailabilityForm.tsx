import { useState, useEffect} from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { AlertCircle, Loader2 } from "lucide-react";
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
} from "../types/teacherAvailability.types";
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";
import { TeacherService } from "@/features/teachers/services/teacherService";
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";
import { InternshipTypeService } from "@/features/internship-types/services/internshipTypeService";

interface TeacherAvailabilityFormProps {
  teacherAvailability?: TeacherAvailability | null;
  onSubmit: (data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function TeacherAvailabilityForm({
  teacherAvailability,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: TeacherAvailabilityFormProps) {
  const { t } = useTranslation("teacherAvailability");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreateTeacherAvailabilityRequest>(() => {
    if (teacherAvailability) {
      return {
        teacherId: teacherAvailability.teacherId,
        academicYearId: teacherAvailability.academicYearId,
        internshipTypeId: teacherAvailability.internshipTypeId,
        isAvailable: teacherAvailability.isAvailable,
        preferenceRank: teacherAvailability.preferenceRank ?? null,
        notes: teacherAvailability.notes ?? "",
      };
    }
    return {
      teacherId: 0,
      academicYearId: 0,
      internshipTypeId: 0,
      isAvailable: true,
      preferenceRank: null,
      notes: "",
    };
  });

  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loadingTeachers, setLoadingTeachers] = useState(true);
  const [loadingAcademicYears, setLoadingAcademicYears] = useState(true);
  const [loadingInternshipTypes, setLoadingInternshipTypes] = useState(true);
  const [errors, setErrors] = useState<Partial<Record<keyof CreateTeacherAvailabilityRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const loadTeachers = async () => {
      try {
        const data = await TeacherService.list({ pageSize: 100 });
        setTeachers(data.items ?? []);
      } catch {
        setTeachers([]);
      } finally {
        setLoadingTeachers(false);
      }
    };
    loadTeachers();
  }, []);

  useEffect(() => {
    const loadAcademicYears = async () => {
      try {
        const data = await AcademicYearService.getAll();
        setAcademicYears(data ?? []);
      } catch {
        setAcademicYears([]);
      } finally {
        setLoadingAcademicYears(false);
      }
    };
    loadAcademicYears();
  }, []);

  useEffect(() => {
    const loadInternshipTypes = async () => {
      try {
        const data = await InternshipTypeService.getAll();
        setInternshipTypes(data ?? []);
      } catch {
        setInternshipTypes([]);
      } finally {
        setLoadingInternshipTypes(false);
      }
    };
    loadInternshipTypes();
  }, []);

  useEffect(() => {
    if (teacherAvailability) {
      setFormData({
        teacherId: teacherAvailability.teacherId,
        academicYearId: teacherAvailability.academicYearId,
        internshipTypeId: teacherAvailability.internshipTypeId,
        isAvailable: teacherAvailability.isAvailable,
        preferenceRank: teacherAvailability.preferenceRank ?? null,
        notes: teacherAvailability.notes ?? "",
      });
    } else {
      setFormData({
        teacherId: 0,
        academicYearId: 0,
        internshipTypeId: 0,
        isAvailable: true,
        preferenceRank: null,
        notes: "",
      });
    }
    setErrors({});
  }, [teacherAvailability]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateTeacherAvailabilityRequest, string>> = {};

    if (!formData.teacherId || formData.teacherId <= 0) {
      newErrors.teacherId = t("form.errors.teacherRequired");
    }
    if (!formData.academicYearId || formData.academicYearId <= 0) {
      newErrors.academicYearId = t("form.errors.academicYearRequired");
    }
    if (!formData.internshipTypeId || formData.internshipTypeId <= 0) {
      newErrors.internshipTypeId = t("form.errors.internshipTypeRequired");
    }
    if (formData.preferenceRank !== null && formData.preferenceRank !== undefined && formData.preferenceRank <= 0) {
      newErrors.preferenceRank = t("form.errors.preferenceRankPositive");
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      if (teacherAvailability) {
        const updateData: UpdateTeacherAvailabilityRequest = {
          isAvailable: formData.isAvailable,
          preferenceRank: formData.preferenceRank,
          notes: formData.notes,
        };
        await onSubmit(updateData);
      } else {
        const createData: CreateTeacherAvailabilityRequest = {
          teacherId: formData.teacherId,
          academicYearId: formData.academicYearId,
          internshipTypeId: formData.internshipTypeId,
          isAvailable: formData.isAvailable,
          preferenceRank: formData.preferenceRank,
          notes: formData.notes,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateTeacherAvailabilityRequest, value: string | number | boolean | null) => {
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
                  {t("form.placeholders.noAcademicYears")}
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
          <label htmlFor="internshipTypeId" className="text-sm font-medium">
            {t("form.fields.internshipType")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.internshipTypeId > 0 ? String(formData.internshipTypeId) : ""}
            onValueChange={(value) => handleChange("internshipTypeId", Number(value))}
            disabled={isLoading || isSubmitting || loadingInternshipTypes}
          >
            <SelectTrigger className={errors.internshipTypeId ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.internshipType")} />
            </SelectTrigger>
            <SelectContent>
              {internshipTypes.length === 0 && !loadingInternshipTypes ? (
                <SelectItem value="" disabled>
                  {t("form.placeholders.noInternshipTypes")}
                </SelectItem>
              ) : (
                internshipTypes.map((type) => (
                  <SelectItem key={type.id} value={String(type.id)}>
                    {type.fullName}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.internshipTypeId && (
            <p className="text-sm text-destructive">{errors.internshipTypeId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="isAvailable" className="text-sm font-medium">
            {t("form.fields.isAvailable")}
            <span className="text-destructive ml-1">*</span>
          </label>
          <Select
            value={formData.isAvailable ? "true" : "false"}
            onValueChange={(value) => handleChange("isAvailable", value === "true")}
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger className={errors.isAvailable ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.isAvailable")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="true">{t("table.available")}</SelectItem>
              <SelectItem value="false">{t("table.notAvailable")}</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2 col-span-1">
          <label htmlFor="preferenceRank" className="text-sm font-medium">
            {t("form.fields.preferenceRank")}
          </label>
          <Input
            id="preferenceRank"
            type="number"
            value={formData.preferenceRank ?? ""}
            onChange={(e) =>
              handleChange("preferenceRank", e.target.value === "" ? null : Number(e.target.value))
            }
            placeholder={t("form.placeholders.preferenceRank")}
            disabled={isLoading || isSubmitting}
            min={1}
          />
          {errors.preferenceRank && (
            <p className="text-sm text-destructive">{errors.preferenceRank}</p>
          )}
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
          ) : teacherAvailability ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}