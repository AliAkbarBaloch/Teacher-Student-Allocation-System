import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { AlertCircle } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type {
  AssignmentStatus,
  CreateTeacherAssignmentRequest,
  TeacherAssignment,
  UpdateTeacherAssignmentRequest,
} from "../types/teacherAssignment.types";

interface TeacherAssignmentFormProps {
  assignment?: TeacherAssignment | null;
  onSubmit: (data: CreateTeacherAssignmentRequest | UpdateTeacherAssignmentRequest) => Promise<void>;
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
  error: externalError = null,
}: TeacherAssignmentFormProps) {
  const { t } = useTranslation("teacherAssignments");
  const { t: tCommon } = useTranslation("common");

  const [formData, setFormData] = useState<CreateTeacherAssignmentRequest>(() => {
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
  });

  const [errors, setErrors] = useState<Partial<Record<keyof CreateTeacherAssignmentRequest, string>>>({});
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
    const newErrors: Partial<Record<keyof CreateTeacherAssignmentRequest, string>> = {};

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
    if (!formData.assignmentStatus || !ASSIGNMENT_STATUS_OPTIONS.includes(formData.assignmentStatus)) {
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

  const handleChange = (field: keyof CreateTeacherAssignmentRequest, value: string | boolean | number) => {
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
          <span>
            {externalError ||
              Object.values(errors)
                .filter(Boolean)
                .join(", ")}
          </span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2 col-span-1">
          <Label htmlFor="planId">{t("form.fields.planId")}</Label>
          <Input
            id="planId"
            type="number"
            value={formData.planId}
            onChange={(e) => handleChange("planId", Number(e.target.value))}
            placeholder={t("form.placeholders.planId")}
            disabled={isLoading || isSubmitting || !!assignment}
            className={errors.planId ? "border-destructive" : ""}
            min={1}
          />
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="teacherId">{t("form.fields.teacherId")}</Label>
          <Input
            id="teacherId"
            type="number"
            value={formData.teacherId}
            onChange={(e) => handleChange("teacherId", Number(e.target.value))}
            placeholder={t("form.placeholders.teacherId")}
            disabled={isLoading || isSubmitting}
            className={errors.teacherId ? "border-destructive" : ""}
            min={1}
          />
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="internshipTypeId">{t("form.fields.internshipTypeId")}</Label>
          <Input
            id="internshipTypeId"
            type="number"
            value={formData.internshipTypeId}
            onChange={(e) => handleChange("internshipTypeId", Number(e.target.value))}
            placeholder={t("form.placeholders.internshipTypeId")}
            disabled={isLoading || isSubmitting}
            className={errors.internshipTypeId ? "border-destructive" : ""}
            min={1}
          />
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="subjectId">{t("form.fields.subjectId")}</Label>
          <Input
            id="subjectId"
            type="number"
            value={formData.subjectId}
            onChange={(e) => handleChange("subjectId", Number(e.target.value))}
            placeholder={t("form.placeholders.subjectId")}
            disabled={isLoading || isSubmitting}
            className={errors.subjectId ? "border-destructive" : ""}
            min={1}
          />
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="studentGroupSize">{t("form.fields.studentGroupSize")}</Label>
          <Input
            id="studentGroupSize"
            type="number"
            value={formData.studentGroupSize}
            onChange={(e) => handleChange("studentGroupSize", Number(e.target.value))}
            placeholder={t("form.placeholders.studentGroupSize")}
            disabled={isLoading || isSubmitting}
            className={errors.studentGroupSize ? "border-destructive" : ""}
            min={1}
          />
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="assignmentStatus">{t("form.fields.assignmentStatus")}</Label>
          <Select
            value={formData.assignmentStatus}
            onValueChange={(value) => handleChange("assignmentStatus", value as AssignmentStatus)}
            disabled={isLoading || isSubmitting}
          >
            <SelectTrigger className={errors.assignmentStatus ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.assignmentStatus")} />
            </SelectTrigger>
            <SelectContent>
              {ASSIGNMENT_STATUS_OPTIONS.map((status) => (
                <SelectItem key={status} value={status}>
                  {t(`form.status.${status.toLowerCase()}`)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.assignmentStatus && (
            <p className="text-sm text-destructive">{errors.assignmentStatus}</p>
          )}
        </div>
        <div className="space-y-2 col-span-1">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isManualOverride"
              checked={formData.isManualOverride}
              onCheckedChange={(checked) => handleChange("isManualOverride", checked === true)}
              disabled={isLoading || isSubmitting}
              className="h-5 w-5"
            />
            <Label
              htmlFor="isManualOverride"
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
            >
              {t("form.fields.isManualOverride")}
            </Label>
          </div>
        </div>
        <div className="space-y-2 col-span-1">
          <Label htmlFor="notes">{t("form.fields.notes")}</Label>
          <Input
            id="notes"
            value={formData.notes}
            onChange={(e) => handleChange("notes", e.target.value)}
            placeholder={t("form.placeholders.notes")}
            disabled={isLoading || isSubmitting}
            className={errors.notes ? "border-destructive" : ""}
            maxLength={5000}
          />
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-4">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={isSubmitting || isLoading}>
          {tCommon("actions.cancel")}
        </Button>
        <Button type="submit" variant="default">
          {assignment ? tCommon("actions.update") : tCommon("actions.create")}
        </Button>
      </div>
    </form>
  );
}