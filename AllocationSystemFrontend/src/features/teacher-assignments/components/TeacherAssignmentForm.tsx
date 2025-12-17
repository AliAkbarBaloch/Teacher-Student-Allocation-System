

import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type {
  AssignmentStatus,
  CreateTeacherAssignmentRequest,
  TeacherAssignment,
  UpdateTeacherAssignmentRequest,
} from "../types/teacherAssignment.types";
import { NumberField } from "@/components/form/fields/NumberField";
import { SelectField } from "@/components/form/fields/SelectField";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { TextAreaField } from "@/components/form/fields/TextAreaField";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { CancelButton } from "@/components/form/button/CancelButton";

interface TeacherAssignmentFormProps {
  assignment?: TeacherAssignment | null;
  onSubmit: (
    data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest
  ) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

const ASSIGNMENT_STATUS_OPTIONS: AssignmentStatus[] = [
  "PLANNED",
  "CONFIRMED",
  "CANCELLED",
  "ON_HOLD",
];

export function TeacherAssignmentForm({
  assignment,
  onSubmit,
  onCancel,
  isLoading = false,
}: TeacherAssignmentFormProps) {
  const { t } = useTranslation("teacherAssignments");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreateTeacherAssignmentRequest>(
    () => {
      if (assignment) {
        return {
          planId: assignment.planId,
          teacherId: assignment.teacherId,
          internshipTypeId: assignment.internshipTypeId,
          subjectId: assignment.subjectId,
          studentGroupSize: assignment.studentGroupSize,
          assignmentStatus: assignment.assignmentStatus,
          isManualOverride: assignment.isManualOverride,
          notes: assignment.notes ?? "",
        };
      }
      return {
        planId: 0,
        teacherId: 0,
        internshipTypeId: 0,
        subjectId: 0,
        studentGroupSize: 1,
        assignmentStatus: "PLANNED",
        isManualOverride: false,
        notes: "",
      };
    }
  );

  const [errors, setErrors] = useState<
    Partial<Record<keyof CreateTeacherAssignmentRequest, string>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (assignment) {
      setFormData({
        planId: assignment.planId,
        teacherId: assignment.teacherId,
        internshipTypeId: assignment.internshipTypeId,
        subjectId: assignment.subjectId,
        studentGroupSize: assignment.studentGroupSize,
        assignmentStatus: assignment.assignmentStatus,
        isManualOverride: assignment.isManualOverride,
        notes: assignment.notes ?? "",
      });
    } else {
      setFormData({
        planId: 0,
        teacherId: 0,
        internshipTypeId: 0,
        subjectId: 0,
        studentGroupSize: 1,
        assignmentStatus: "PLANNED",
        isManualOverride: false,
        notes: "",
      });
    }
    setErrors({});
  }, [assignment]);

  const validate = (): boolean => {
    const newErrors: Partial<
      Record<keyof CreateTeacherAssignmentRequest, string>
    > = {};

    if (!formData.planId || formData.planId <= 0) {
      newErrors.planId = t("form.errors.planRequired");
    }
    if (!formData.teacherId || formData.teacherId <= 0) {
      newErrors.teacherId = t("form.errors.teacherRequired");
    }
    if (!formData.internshipTypeId || formData.internshipTypeId <= 0) {
      newErrors.internshipTypeId = t("form.errors.internshipTypeRequired");
    }
    if (!formData.subjectId || formData.subjectId <= 0) {
      newErrors.subjectId = t("form.errors.subjectRequired");
    }
    if (!formData.studentGroupSize || formData.studentGroupSize < 1) {
      newErrors.studentGroupSize = t("form.errors.groupSizeMin");
    }
    if (
      !formData.assignmentStatus ||
      !ASSIGNMENT_STATUS_OPTIONS.includes(formData.assignmentStatus)
    ) {
      newErrors.assignmentStatus = t("form.errors.statusRequired");
    }
    if (formData.notes && formData.notes.length > 5000) {
      newErrors.notes = t("form.errors.notesMaxLength");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      if (assignment) {
        // For update, only send updatable fields
        const updateData: UpdateTeacherAssignmentRequest = {
          studentGroupSize: formData.studentGroupSize,
          assignmentStatus: formData.assignmentStatus,
          isManualOverride: formData.isManualOverride,
          notes: formData.notes?.trim() || "",
        };
        await onSubmit(updateData);
      } else {
        // For create, send all required fields
        const createData: CreateTeacherAssignmentRequest = {
          ...formData,
          notes: formData.notes?.trim() || "",
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (
    field: keyof CreateTeacherAssignmentRequest,
    value: string | boolean | number
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 pb-2">
      <div className="grid gap-4 md:grid-cols-2">
        <NumberField
          id="planId"
          label={t("form.fields.planId")}
          value={formData.planId}
          onChange={(val: number) => handleChange("planId", val)}
          placeholder={t("form.placeholders.planId")}
          disabled={isLoading || isSubmitting || !!assignment}
          error={errors.planId}
          min={1}
        />

        <NumberField
          id="teacherId"
          label={t("form.fields.teacherId")}
          value={formData.teacherId}
          onChange={(val: number) => handleChange("teacherId", val)}
          placeholder={t("form.placeholders.teacherId")}
          disabled={isLoading || isSubmitting || !!assignment}
          error={errors.teacherId}
          min={1}
        />
        <NumberField
          id="internshipTypeId"
          label={t("form.fields.internshipTypeId")}
          value={formData.internshipTypeId}
          onChange={(val: number) => handleChange("internshipTypeId", val)}
          placeholder={t("form.placeholders.internshipTypeId")}
          disabled={isLoading || isSubmitting || !!assignment}
          error={errors.internshipTypeId}
          min={1}
        />

        <NumberField
          id="subjectId"
          label={t("form.fields.subjectId")}
          value={formData.subjectId}
          onChange={(val: number) => handleChange("subjectId", val)}
          placeholder={t("form.placeholders.subjectId")}
          disabled={isLoading || isSubmitting || !!assignment}
          error={errors.subjectId}
          min={1}
        />

        <NumberField
          id="studentGroupSize"
          label={t("form.fields.studentGroupSize")}
          value={formData.studentGroupSize}
          onChange={(val: number) => handleChange("studentGroupSize", val)}
          placeholder={t("form.placeholders.studentGroupSize")}
          disabled={isLoading || isSubmitting}
          error={errors.studentGroupSize}
          min={1}
        />
        <SelectField
          id="assignmentStatus"
          label={t("form.fields.assignmentStatus")}
          value={formData.assignmentStatus}
          onChange={(val: string) =>
            handleChange("assignmentStatus", val as AssignmentStatus)
          }
          options={ASSIGNMENT_STATUS_OPTIONS.map((status) => ({
            value: status,
            label: t(`form.status.${status.toLowerCase()}`),
          }))}
          placeholder={t("form.placeholders.assignmentStatus")}
          disabled={isLoading || isSubmitting}
          error={errors.assignmentStatus}
        />
        <div className="space-y-2 col-span-1">
          <CheckboxField
            id="isManualOverride"
            label={t("form.fields.isManualOverride")}
            checked={!!formData.isManualOverride}
            onCheckedChange={(checked: boolean) =>
              handleChange("isManualOverride", checked)
            }
            disabled={isLoading || isSubmitting}
            className="lg:mt-7"
            labelClassName="mt-1.5"
          />
        </div>
        <div className="space-y-2 col-span-1">
          <TextAreaField
            id="notes"
            label={t("form.fields.notes")}
            value={formData.notes ?? ""}
            onChange={(val: string) => handleChange("notes", val)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            error={errors.notes}
            maxLength={500}
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
          isEdit={!!assignment}
          createText={tCommon("actions.create")}
          updateText={tCommon("actions.update")}
          savingText={tCommon("actions.saving")}
          disabled={isLoading || isSubmitting}
        />

      </div>
    </form>
  );
}
