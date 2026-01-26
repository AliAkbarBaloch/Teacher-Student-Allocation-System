// translations
import { useTranslation } from "react-i18next";
// icons
import { AlertCircle } from "lucide-react";

// types
import type {
  AllocationPlan,
  CreateAllocationPlanRequest,
  UpdateAllocationPlanRequest,
  PlanStatus,
} from "../types/allocationPlan.types";

import { useAllocationPlanFormLogic } from "./AllocationPlanForm.logic";
import { PLAN_STATUS_OPTIONS } from "./AllocationPlanForm.helper";
import { CancelButton } from "@/components/form/button/CancelButton";
import { SubmitButton } from "@/components/form/button/SubmitButton";
import { SelectField } from "@/components/form/fields/SelectField";
import { TextField } from "@/components/form/fields/TextField";
import { CheckboxField } from "@/components/form/fields/CheckboxField";
import { TextAreaField } from "@/components/form/fields/TextAreaField";


function AllocationPlanFormErrorBanner(props: { text?: string | null }) {
  if (!props.text) {
    return null;
  }

  return (
    <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
      <AlertCircle className="h-4 w-4" />
      <span>{props.text}</span>
    </div>
  );
}
function AllocationPlanFormActions(props: { disabled: boolean; isEdit: boolean; onCancel: () => void; tCommon: (k: string) => string }) {
  return (
    <div className="flex justify-end gap-2 pt-2">
      <CancelButton onClick={props.onCancel} disabled={props.disabled}>
        {props.tCommon("actions.cancel")}
      </CancelButton>
      <SubmitButton
        isLoading={props.disabled}
        isEdit={props.isEdit}
        createText={props.tCommon("actions.create")}
        updateText={props.tCommon("actions.update")}
        savingText={props.tCommon("actions.saving")}
        disabled={props.disabled}
      />
    </div>
  );
}

/**
 * Props for allocation plan form field sections.
 */
interface AllocationPlanFormFieldsProps {
  formData: CreateAllocationPlanRequest;
  errors: Partial<Record<keyof CreateAllocationPlanRequest, string>>;
  disabled: boolean;
  loadingYears: boolean;
  yearValue: string;
  yearOptions: { value: string; label: string }[];
  onChange: (field: keyof CreateAllocationPlanRequest, value: string | number | boolean | null) => void;
  t: (key: string) => string;
}

function AllocationPlanYearField({ disabled, loadingYears, yearValue, yearOptions, errors, onChange, t }: AllocationPlanFormFieldsProps) {
  return (
    <SelectField
      id="yearId"
      label={t("form.fields.yearId")}
      value={yearValue}
      onChange={(val: string) => onChange("yearId", val === "__none__" ? 0 : Number(val))}
      options={yearOptions}
      placeholder={t("form.placeholders.yearId")}
      disabled={disabled || loadingYears}
      error={errors.yearId}
    />
  );
}

function AllocationPlanNameVersionFields({ formData, errors, disabled, onChange, t }: AllocationPlanFormFieldsProps) {
  return (
    <>
      <TextField
        id="planName"
        label={t("form.fields.planName")}
        value={formData.planName}
        onChange={(val: string) => onChange("planName", val)}
        placeholder={t("form.placeholders.planName")}
        disabled={disabled}
        error={errors.planName}
        maxLength={255}
      />

      <TextField
        id="planVersion"
        label={t("form.fields.planVersion")}
        value={formData.planVersion}
        onChange={(val: string) => onChange("planVersion", val)}
        placeholder={t("form.placeholders.planVersion")}
        disabled={disabled}
        error={errors.planVersion}
        maxLength={100}
      />
    </>
  );
}

function AllocationPlanStatusField({ formData, errors, disabled, onChange, t }: AllocationPlanFormFieldsProps) {
  return (
    <SelectField
      id="status"
      label={t("form.fields.status")}
      value={formData.status ?? "DRAFT"}
      onChange={(val: string) => onChange("status", val as PlanStatus)}
      options={PLAN_STATUS_OPTIONS}
      placeholder={t("form.placeholders.status")}
      disabled={disabled}
      error={errors.status}
    />
  );
}

function AllocationPlanMainFields(props: AllocationPlanFormFieldsProps) {
  return (
    <>
      <AllocationPlanNameVersionFields {...props} />
      <AllocationPlanStatusField {...props} />
    </>
  );
}

function AllocationPlanFlagsAndNotes({ formData, errors, disabled, onChange, t }: AllocationPlanFormFieldsProps) {
  return (
    <>
      <CheckboxField
        id="isCurrent"
        label={t("form.fields.isCurrent")}
        checked={!!formData.isCurrent}
        onCheckedChange={(checked) => onChange("isCurrent", checked === true)}
        disabled={disabled}
        labelClassName="mt-1.5"
        className="lg:mt-7"
      />

      <TextAreaField
        id="notes"
        label={t("form.fields.notes")}
        value={formData.notes ?? ""}
        onChange={(val: string) => onChange("notes", val)}
        placeholder={t("form.placeholders.notes")}
        disabled={disabled}
        error={errors.notes}
        maxLength={500}
      />
    </>
  );
}

/**
 * Renders the input fields for the allocation plan form.
 */
function AllocationPlanFormFields(props: AllocationPlanFormFieldsProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <AllocationPlanYearField {...props} />
      <AllocationPlanMainFields {...props} />
      <AllocationPlanFlagsAndNotes {...props} />
    </div>
  );
}

/**
 * Props for the AllocationPlanForm component.
 */
interface AllocationPlanFormProps {
  allocationPlan?: AllocationPlan | null;
  onSubmit: (data: CreateAllocationPlanRequest | UpdateAllocationPlanRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

/**
 * Renders the main body of the allocation plan form.
 */
function AllocationPlanFormContent(props: {
  formData: CreateAllocationPlanRequest;
  errors: Partial<Record<keyof CreateAllocationPlanRequest, string>>;
  disabled: boolean;
  loadingYears: boolean;
  yearValue: string;
  yearOptions: { value: string; label: string }[];
  onChange: (field: keyof CreateAllocationPlanRequest, value: string | number | boolean | null) => void;
  t: (key: string) => string;
}) {
  return (
    <AllocationPlanFormFields
      formData={props.formData}
      errors={props.errors}
      disabled={props.disabled}
      loadingYears={props.loadingYears}
      yearValue={props.yearValue}
      yearOptions={props.yearOptions}
      onChange={props.onChange}
      t={props.t}
    />
  );
}

const getBannerText = (
  externalError: string | null,
  errors: Partial<Record<string, unknown>>
): string | null =>
  externalError ?? Object.values(errors).find((v) => typeof v === "string") ?? null;

export function AllocationPlanForm({
  allocationPlan,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: AllocationPlanFormProps) {
  const { t } = useTranslation("allocationPlans");
  const { t: tCommon } = useTranslation("common");

  const logic = useAllocationPlanFormLogic({ allocationPlan, isLoading, onSubmit, t });

  const bannerText = getBannerText(externalError, logic.errors);

  return (
    <form onSubmit={logic.handleSubmit} className="space-y-4 py-4">
      <AllocationPlanFormErrorBanner text={bannerText} />

      <AllocationPlanFormContent
        formData={logic.formData}
        errors={logic.errors}
        disabled={logic.disabled}
        loadingYears={logic.loadingYears}
        yearValue={logic.yearValue}
        yearOptions={logic.yearOptions}
        onChange={logic.handleChange}
        t={t}
      />

      <AllocationPlanFormActions
        disabled={logic.disabled}
        isEdit={!!allocationPlan}
        onCancel={onCancel}
        tCommon={tCommon}
      />
    </form>
  );
}

