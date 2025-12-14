import { useState, useEffect } from "react";
// translations
import { useTranslation } from "react-i18next";
//types
import type {
  TeacherAvailability,
  CreateTeacherAvailabilityRequest,
  UpdateTeacherAvailabilityRequest,
  AvailabilityStatus,
} from "../types/teacherAvailability.types";
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { AcademicYear } from "@/features/academic-years/types/academicYear.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";
import { TeacherService } from "@/features/teachers/services/teacherService";

// services
import { AcademicYearService } from "@/features/academic-years/services/academicYearService";
import { InternshipTypeService } from "@/features/internship-types/services/internshipTypeService";

// components
import { SelectField } from "@/components/form/fields/SelectField";
import { NumberField } from "@/components/form/fields/NumberField";
import { TextAreaField } from "@/components/form/fields/TextAreaField";
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";

interface TeacherAvailabilityFormProps {
  teacherAvailability?: TeacherAvailability | null;
  onSubmit: (
    data: CreateTeacherAvailabilityRequest | UpdateTeacherAvailabilityRequest
  ) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function TeacherAvailabilityForm({
  teacherAvailability,
  onSubmit,
  onCancel,
  isLoading = false,
}: TeacherAvailabilityFormProps) {
  const { t } = useTranslation("teacherAvailability");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<
    CreateTeacherAvailabilityRequest & { status: AvailabilityStatus }
  >(() => {
    if (teacherAvailability) {
      return {
        teacherId: teacherAvailability.teacherId,
        academicYearId: teacherAvailability.academicYearId,
        internshipTypeId: teacherAvailability.internshipTypeId,
        status: teacherAvailability.status || "AVAILABLE",
        preferenceRank: teacherAvailability.preferenceRank ?? null,
        notes: teacherAvailability.notes ?? "",
      };
    }
    return {
      teacherId: 0,
      academicYearId: 0,
      internshipTypeId: 0,
      status: "AVAILABLE",
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
  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateTeacherAvailabilityRequest, string>>
  >({});
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
        status: teacherAvailability.status || "AVAILABLE",
        preferenceRank: teacherAvailability.preferenceRank ?? null,
        notes: teacherAvailability.notes ?? "",
      });
    } else {
      setFormData({
        teacherId: 0,
        academicYearId: 0,
        internshipTypeId: 0,
        status: "AVAILABLE",
        preferenceRank: null,
        notes: "",
      });
    }
    setErrors({});
  }, [teacherAvailability]);

  const validate = (): boolean => {
    const newErrors: Partial<
      Record<keyof CreateTeacherAvailabilityRequest, string>
    > = {};

    if (!formData.teacherId || formData.teacherId <= 0) {
      newErrors.teacherId = t("form.errors.teacherRequired");
    }
    if (!formData.academicYearId || formData.academicYearId <= 0) {
      newErrors.academicYearId = t("form.errors.academicYearRequired");
    }
    if (!formData.internshipTypeId || formData.internshipTypeId <= 0) {
      newErrors.internshipTypeId = t("form.errors.internshipTypeRequired");
    }
    if (!formData.status) {
      newErrors.status = t("form.errors.isAvailableRequired");
    }
    if (
      formData.preferenceRank !== null &&
      formData.preferenceRank !== undefined &&
      formData.preferenceRank <= 0
    ) {
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
          status: formData.status,
          preferenceRank: formData.preferenceRank,
          notes: formData.notes,
        };
        await onSubmit(updateData);
      } else {
        const createData: CreateTeacherAvailabilityRequest = {
          teacherId: formData.teacherId,
          academicYearId: formData.academicYearId,
          internshipTypeId: formData.internshipTypeId,
          status: formData.status,
          preferenceRank: formData.preferenceRank,
          notes: formData.notes,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    field: keyof CreateTeacherAvailabilityRequest,
    value: string | number | boolean | null | AvailabilityStatus
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 pb-2">
      <div className="grid gap-4 md:grid-cols-2">
        <SelectField
          id="teacherId"
          label={t("form.fields.teacher")}
          value={formData.teacherId > 0 ? String(formData.teacherId) : ""}
          onChange={(value: string) => handleChange("teacherId", Number(value))}
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.teacher"),
              disabled: true,
            },
            ...teachers.map((teacher) => ({
              value: String(teacher.id),
              label: `${teacher.firstName} ${teacher.lastName} (${teacher.email})`,
            })),
          ]}
          placeholder={t("form.placeholders.teacher")}
          disabled={isLoading || isSubmitting || loadingTeachers}
          error={errors.teacherId}
        />
        <SelectField
          id="academicYearId"
          label={t("form.fields.academicYear")}
          value={
            formData.academicYearId > 0 ? String(formData.academicYearId) : ""
          }
          onChange={(value: string) =>
            handleChange("academicYearId", Number(value))
          }
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.academicYear"),
              disabled: true,
            },
            ...academicYears.map((year) => ({
              value: String(year.id),
              label: year.yearName,
            })),
          ]}
          placeholder={t("form.placeholders.academicYear")}
          disabled={isLoading || isSubmitting || loadingAcademicYears}
          error={errors.academicYearId}
        />

        <SelectField
          id="internshipTypeId"
          label={t("form.fields.internshipType")}
          value={
            formData.internshipTypeId > 0
              ? String(formData.internshipTypeId)
              : ""
          }
          onChange={(value: string) =>
            handleChange("internshipTypeId", Number(value))
          }
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.internshipType"),
              disabled: true,
            },
            ...internshipTypes.map((type) => ({
              value: String(type.id),
              label: type.fullName,
            })),
          ]}
          placeholder={t("form.placeholders.internshipType")}
          disabled={isLoading || isSubmitting || loadingInternshipTypes}
          error={errors.internshipTypeId}
        />

        <SelectField
          id="status"
          label={t("form.fields.isAvailable")}
          value={formData.status ?? ""}
          onChange={(value: string) => handleChange("status", value)}
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.isAvailable"),
              disabled: true,
            },
            { value: "AVAILABLE", label: t("table.available") },
            { value: "PREFERRED", label: t("table.preferred") },
            { value: "NOT_AVAILABLE", label: t("table.notAvailable") },
            { value: "BACKUP_ONLY", label: t("table.backupOnly") },
          ]}
          placeholder={t("form.placeholders.isAvailable")}
          disabled={isLoading || isSubmitting}
          error={errors.status}
        />

        <NumberField
          id="preferenceRank"
          label={t("form.fields.preferenceRank")}
          value={formData.preferenceRank ?? ""}
          onChange={(value: number) => handleChange("preferenceRank", value)}
          placeholder={t("form.placeholders.preferenceRank")}
          disabled={isLoading || isSubmitting}
          error={errors.preferenceRank}
        />
        <div className="col-span-2">
          <TextAreaField
            id="notes"
            label={t("form.fields.notes")}
            value={formData.notes ?? ""}
            onChange={(value: string) => handleChange("notes", value)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            error={errors.notes}
            maxLength={500}
            rows={4}
          />
        </div>
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
          isEdit={!!teacherAvailability}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />
      </div>
    </form>
  );
}
