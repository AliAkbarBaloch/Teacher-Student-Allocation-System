
// types
import type {
  CreateTeacherRequest,
  Teacher,
  UpdateTeacherRequest,
} from "../types/teacher.types";

// hooks
import { useTeacherForm } from "../hooks/useTeacherForm";
import { useMemo, useEffect } from "react";

// translations
import { useTranslation } from "react-i18next";

// components
import { SelectField } from "@/components/form/fields/SelectField";
import { TextField } from "@/components/form/fields/TextField";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { NumberField } from "@/components/form/fields/NumberField";
import { Checkbox } from "@/components/ui/checkbox";

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
  const {
    mode,
    onSubmit,
    onCancel,
    isSubmitting = false,
    readOnly = false,
  } = props;
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
    subjects, 
    subjectsLoading,
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
      <div className="grid gap-4 md:grid-cols-2">
        <SelectField
          id="schoolId"
          label={t("form.fields.school")}
          value={schoolIdValue || ""}
          onChange={(value) => handleInputChange("schoolId", value || "")}
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.school"),
              disabled: true,
            },
            ...schools.map((school) => ({
              value: school.id.toString(),
              label: school.schoolName,
            })),
          ]}
          placeholder={t("form.placeholders.school")}
          disabled={isDisabled || loadingSchools}
          error={errors.schoolId}
          required={true}
        />
        {/* <div className="space-y-2">
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
        </div> */}

        <SelectField
          id="employmentStatus"
          label={t("form.fields.employmentStatus")}
          value={employmentStatusValue || ""}
          onChange={(value) =>
            handleInputChange("employmentStatus", value || "")
          }
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.employmentStatus"),
              disabled: true,
            },
            ...employmentStatusOptions.map((option) => ({
              value: option.value,
              label: option.label,
            })),
          ]}
          placeholder={t("form.placeholders.employmentStatus")}
          disabled={isDisabled}
          error={errors.employmentStatus}
          required={true}
        />
        <TextField
          id="firstName"
          label={t("form.fields.firstName")}
          value={formState.firstName}
          onChange={(value) => handleInputChange("firstName", value || "")}
          placeholder={t("form.placeholders.firstName")}
          disabled={isDisabled}
          error={errors.firstName}
          required={true}
        />

        <TextField
          id="lastName"
          label={t("form.fields.lastName")}
          value={formState.lastName}
          onChange={(value) => handleInputChange("lastName", value || "")}
          placeholder={t("form.placeholders.lastName")}
          disabled={isDisabled}
          error={errors.lastName}
          required={true}
        />

        <TextField
          id="email"
          label={t("form.fields.email")}
          value={formState.email}
          onChange={(value) => handleInputChange("email", value || "")}
          placeholder={t("form.placeholders.email")}
          disabled={isDisabled}
          error={errors.email}
          required={true}
        />

        <TextField
          id="phone"
          label={t("form.fields.phone")}
          value={formState.phone}
          onChange={(value) => handleInputChange("phone", value || "")}
          placeholder={t("form.placeholders.phone")}
          disabled={isDisabled}
          error={errors.phone}
        />

        <CheckboxField
          id="isPartTime"
          label={t("form.fields.isPartTime")}
          checked={formState.isPartTime}
          onCheckedChange={(checked) =>
            handleInputChange("isPartTime", Boolean(checked))
          }
          disabled={isDisabled}
          description={t("form.helpers.isPartTime")}
        />

        <SelectField
          id="usageCycle"
          label={t("form.fields.usageCycle")}
          value={usageCycleValue || ""}
          onChange={(value) => handleInputChange("usageCycle", value || "")}
          options={[
            {
              value: "__placeholder__",
              label: t("form.placeholders.usageCycle"),
              disabled: true,
            },
            ...usageCycleOptions.map((option) => ({
              value: option.value,
              label: option.label,
            })),
          ]}
          placeholder={t("form.placeholders.usageCycle")}
          disabled={isDisabled}
          error={errors.usageCycle}
        />

        {formState.isPartTime && (
          <NumberField
            id="workingHoursPerWeek"
            label={t("form.fields.workingHoursPerWeek")}
            value={String(formState.workingHoursPerWeek ?? "")}
            onChange={(val) => handleInputChange("workingHoursPerWeek", val != null ? String(val) : "")}
            error={errors.workingHoursPerWeek}
            min={1}
            required
            disabled={isDisabled}
          />
        )}

        {/* Subjects */}

        <div className="space-y-2 md:col-span-2">
          {/* title for subjects checkboxes */}
          <label className="text-sm font-medium">
            {t("form.fields.subjects")}
          </label>

          {/*conditional rendering*/}

          {/*if subjects are still loading show Loading subjects*/}
          {subjectsLoading ? (
            <p className="text-sm text-muted-foreground">
              {t("form.loadingSubjects", {defaultValue: "Loading subjects..."})}
            </p>  
          //else if subjects exists - show checkboxes
          ) : subjects.length ? ( 
            <div className="grid grid-cols-2 gap-3">

              {/*for every subject we recieved from the backend draw checkbox*/}
              {subjects.map((subject) => {

                //is this subject already checked 
                const checked = formState.subjectIds.includes(subject.id);

                return (
                  <div 
                    key = {subject.id}
                    className="flex items-center gap-2 rounded-md border px-3 py-2"
                  >
                  <Checkbox
                    checked = {checked}
                    disabled = {isDisabled}
                    id = {`subject-${subject.id}`}
                    
                    onCheckedChange={(value) => {
                        const nextIds = value
                        //check - add the subject to checked 
                        ? [...formState.subjectIds, subject.id]
                        // uncheck - delete 
                        : formState.subjectIds.filter((id) => id !== subject.id);
                        //update the form state
                        handleInputChange("subjectIds", nextIds);
                      }}
                    />
                  <label
                  htmlFor = {`subject-${subject.id}`}
                  className = "text-sm cursor-pointer select-none"
                  >
                    {subject.subjectTitle}
                  </label>

                </div>
                );
              }
              )}
          </div>
          //else = no subjects at all - show "no data"
          ) : (
            <p className="text-sm text-muted-foreground">{t("table.noData")}</p>
          )
        }
      </div>

      {!readOnly && (
        <div className="flex justify-end gap-2">
          <CancelButton onClick={onCancel} disabled={isDisabled}>
            {tCommon("actions.cancel")}
          </CancelButton>
          <SubmitButton
            isLoading={isSubmitting || internalSubmitting}
            isEdit={mode === "edit"}
            createText={tCommon("actions.create")}
            updateText={tCommon("actions.update")}
            savingText={tCommon("actions.saving")}
            disabled={isDisabled}
          />
        </div>
      )}
      </div>
    </form>
  );
}
