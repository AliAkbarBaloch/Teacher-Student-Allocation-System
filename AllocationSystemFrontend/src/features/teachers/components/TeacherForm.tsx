// components
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

// types
import type { CreateTeacherRequest, Teacher, UpdateTeacherRequest } from "../types/teacher.types";

// hooks
import { useTeacherForm } from "../hooks/useTeacherForm";
import { useMemo, useEffect } from "react";

// translations
import { useTranslation } from "react-i18next";

// icons
import { AlertCircle, Loader2 } from "lucide-react";

type BaseTeacherFormProps = {
  onCancel: () => void;
  isSubmitting?: boolean;
  readOnly?: boolean;
};

type CreateTeacherFormProps = BaseTeacherFormProps & {
  mode: "create";
  teacher?: null;
  onSubmit: (values: CreateTeacherRequest) => Promise<void>;
};

type EditTeacherFormProps = BaseTeacherFormProps & {
  mode: "edit";
  teacher: Teacher;
  onSubmit: (values: UpdateTeacherRequest) => Promise<void>;
};

type TeacherFormProps = CreateTeacherFormProps | EditTeacherFormProps;

export function TeacherForm(props: TeacherFormProps) {
  const { mode, onSubmit, onCancel, isSubmitting = false, readOnly = false } = props;
  const { t } = useTranslation("teachers");
  const { t: tCommon } = useTranslation("common");
  const hookProps =
    mode === "edit"
      ? { mode, onSubmit, teacher: props.teacher, t }
      : ({ mode, onSubmit, t } as const);

  const {
    formState,
    errors,
    generalError,
    employmentStatusOptions,
    usageCycleOptions,
    schools,
    loadingSchools,
    handleInputChange,
    handleSubmit,
    internalSubmitting,
  } = useTeacherForm(hookProps);

  const isDisabled = isSubmitting || internalSubmitting || readOnly;

  // Normalize Select values to always be undefined when empty (for controlled components)
  // Ensure consistent value type to prevent controlled/uncontrolled switching
  // Always return undefined (not null or empty string) when there's no value
  const schoolIdValue = useMemo((): string | undefined => {
    const trimmed = formState.schoolId?.trim();
    return trimmed && trimmed.length > 0 ? trimmed : undefined;
  }, [formState.schoolId]);

  const employmentStatusValue = useMemo((): string | undefined => {
    const trimmed = formState.employmentStatus?.trim();
    return trimmed && trimmed.length > 0 ? trimmed : undefined;
  }, [formState.employmentStatus]);

  const usageCycleValue = useMemo((): string | undefined => {
    const trimmed = formState.usageCycle?.trim();
    return trimmed && trimmed.length > 0 ? trimmed : undefined;
  }, [formState.usageCycle]);

  // Collect all error messages for display
  const allErrors = useMemo(() => {
    const fieldErrors = Object.entries(errors)
      .filter(([, message]) => message)
      .map(([field, message]) => ({ field, message: message as string }));
    return { generalError, fieldErrors };
  }, [errors, generalError]);

  // Debug: Log errors when they change
  useEffect(() => {
    if (generalError || Object.keys(errors).length > 0) {
      console.log("Form errors state:", { generalError, errors, allErrors });
    }
  }, [generalError, errors, allErrors]);

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {(allErrors.generalError || allErrors.fieldErrors.length > 0) && (
        <div className="rounded-md border border-destructive/20 bg-destructive/10 p-4 space-y-2">
          {allErrors.generalError && (
            <div className="flex items-start gap-2 text-sm text-destructive">
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" />
              <span>{allErrors.generalError}</span>
            </div>
          )}
          {allErrors.fieldErrors.length > 0 && (
            <div className="space-y-1">
              {allErrors.fieldErrors.map(({ field, message }) => (
                <div
                  key={field}
                  className="flex items-start gap-2 text-sm text-destructive"
                >
                  <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" />
                  <span>
                    <span className="font-medium capitalize">
                      {field.replace(/([A-Z])/g, " $1").trim()}:
                    </span>{" "}
                    {message}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="schoolId">
            {t("form.fields.school")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={schoolIdValue}
            onValueChange={(value) =>
              handleInputChange("schoolId", value || "")
            }
            disabled={isDisabled || loadingSchools}
          >
            <SelectTrigger
              className={`w-full ${
                errors.schoolId ? "border-destructive" : ""
              }`}
            >
              <SelectValue
                placeholder={
                  loadingSchools
                    ? t("form.loadingSchools")
                    : t("form.placeholders.school")
                }
              />
            </SelectTrigger>
            <SelectContent className="max-w-md">
              {(schools || []).map((school) => (
                <SelectItem
                  key={school.id}
                  value={String(school.id)}
                  className="whitespace-normal"
                >
                  {school.schoolName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.schoolId && (
            <p className="text-sm text-destructive">{errors.schoolId}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="employmentStatus">
            {t("form.fields.employmentStatus")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={employmentStatusValue}
            onValueChange={(value) =>
              handleInputChange("employmentStatus", value || "")
            }
            disabled={isDisabled}
          >
            <SelectTrigger
              className={errors.employmentStatus ? "border-destructive" : ""}
            >
              <SelectValue
                placeholder={t("form.placeholders.employmentStatus")}
              />
            </SelectTrigger>
            <SelectContent>
              {employmentStatusOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.employmentStatus && (
            <p className="text-sm text-destructive">
              {errors.employmentStatus}
            </p>
          )}
        </div>
        <div className="space-y-2">
          <Label htmlFor="firstName">
            {t("form.fields.firstName")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="firstName"
            value={formState.firstName}
            onChange={(event) =>
              handleInputChange("firstName", event.target.value)
            }
            placeholder={t("form.placeholders.firstName")}
            disabled={isDisabled}
            className={errors.firstName ? "border-destructive" : ""}
          />
          {errors.firstName && (
            <p className="text-sm text-destructive">{errors.firstName}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="lastName">
            {t("form.fields.lastName")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="lastName"
            value={formState.lastName}
            onChange={(event) =>
              handleInputChange("lastName", event.target.value)
            }
            placeholder={t("form.placeholders.lastName")}
            disabled={isDisabled}
            className={errors.lastName ? "border-destructive" : ""}
          />
          {errors.lastName && (
            <p className="text-sm text-destructive">{errors.lastName}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="email">
            {t("form.fields.email")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="email"
            type="email"
            value={formState.email}
            onChange={(event) => handleInputChange("email", event.target.value)}
            placeholder={t("form.placeholders.email")}
            disabled={isDisabled}
            className={errors.email ? "border-destructive" : ""}
          />
          {errors.email && (
            <p className="text-sm text-destructive">{errors.email}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="phone">{t("form.fields.phone")}</Label>
          <Input
            id="phone"
            value={formState.phone}
            onChange={(event) => handleInputChange("phone", event.target.value)}
            placeholder={t("form.placeholders.phone")}
            disabled={isDisabled}
            className={errors.phone ? "border-destructive" : ""}
          />
          {errors.phone && (
            <p className="text-sm text-destructive">{errors.phone}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label
            htmlFor="isPartTime"
            className="flex items-center gap-2 rounded-md border border-muted px-4 py-3 cursor-pointer hover:bg-accent/50 transition-colors has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10"
          >
            <Checkbox
              id="isPartTime"
              checked={formState.isPartTime}
              onCheckedChange={(checked) =>
                handleInputChange("isPartTime", Boolean(checked))
              }
              disabled={isDisabled}
            />
            <div>
              <span className="font-medium">
                {t("form.fields.isPartTime")}
              </span>
              <p className="text-xs text-muted-foreground">
                {t("form.helpers.isPartTime")}
              </p>
            </div>
          </Label>
          {errors.isPartTime && (
            <p className="text-sm text-destructive">{errors.isPartTime}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="usageCycle">{t("form.fields.usageCycle")}</Label>
          <Select
            value={usageCycleValue}
            onValueChange={(value) => {
              // Handle "none" as a special value to clear the selection
              handleInputChange(
                "usageCycle",
                value === "__none__" ? "" : value || ""
              );
            }}
            disabled={isDisabled}
          >
            <SelectTrigger>
              <SelectValue placeholder={t("form.placeholders.usageCycle")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.usageCycle")}
              </SelectItem>
              {usageCycleOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {!readOnly && (
        <div className="flex justify-end gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isDisabled}
          >
            {tCommon("actions.cancel")}
          </Button>
          <Button type="submit" disabled={isDisabled}>
            {isDisabled ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {tCommon("actions.saving")}
              </>
            ) : mode === "edit" ? (
              tCommon("actions.update")
            ) : (
              tCommon("actions.create")
            )}
          </Button>
        </div>
      )}
    </form>
  );
}

