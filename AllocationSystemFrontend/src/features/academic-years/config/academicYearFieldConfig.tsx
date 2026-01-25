import type { FieldConfig } from "@/components/common/types/form.types";
import type { AcademicYear } from "../types/academicYear.types";
import type { TFunction } from "i18next";

const EMPTY_VIEW = "—";

/**
 * Converts an ISO date string into a human readable string.
 */
function formatDateTimeForView(value: unknown): string {
  if (!value || typeof value !== "string") {
    return EMPTY_VIEW;
  }
  try {
    return new Date(value).toLocaleString();
  } catch {
    return String(value);
  }
}

/**
 * Converts an ISO string to datetime-local format (YYYY-MM-DDTHH:mm) for inputs.
 */
function isoToDateTimeLocal(value: unknown): string {
  if (!value || typeof value !== "string") {
    return "";
  }

  const date = new Date(value);
  if (isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Converts datetime-local input string to ISO string.
 */
function dateTimeLocalToIso(value: unknown): string {
  if (!value || typeof value !== "string") {
    return "";
  }
  return new Date(value).toISOString();
}

/**
 * Converts datetime-local input string to ISO string, but returns null if empty.
 */
function dateTimeLocalToIsoOrNull(value: unknown): string | null {
  if (!value || typeof value !== "string") {
    return null;
  }
  return new Date(value).toISOString();
}

function viewStringOrDash(value: unknown): string {
  return (value as string) || EMPTY_VIEW;
}

function viewValueOrDash(value: unknown): string {
  return String(value ?? EMPTY_VIEW);
}

function makeNumberField(
  t: TFunction<"academicYears">,
  name: "totalCreditHours" | "elementarySchoolHours" | "middleSchoolHours"
): FieldConfig<AcademicYear> {
  return {
    name,
    type: "number",
    label: t(`form.fields.${name}`),
    placeholder: t(`form.placeholders.${name}`),
    required: true,
    validation: {
      required: true,
      min: 0,
    },
    min: 0,
    viewFormat: viewStringOrDash,
  };
}

function makeDateTimeField(
  t: TFunction<"academicYears">,
  name: "budgetAnnouncementDate" | "allocationDeadline",
  required: boolean
): FieldConfig<AcademicYear> {
  let validation: { required: true } | undefined;
  let output: (value: unknown) => string | null;

  if (required) {
    validation = { required: true };
    output = dateTimeLocalToIso;
  } else {
    validation = undefined;
    output = dateTimeLocalToIsoOrNull;
  }

  return {
    name,
    type: "datetime-local",
    label: t(`form.fields.${name}`),
    placeholder: t(`form.placeholders.${name}`),
    required,
    validation,
    transform: {
      input: isoToDateTimeLocal,
      output,
    },
    viewFormat: formatDateTimeForView,
  };
}


function formatLockedStatus(t: TFunction<"academicYears">, data: AcademicYear): string {
  if (data.isLocked) {
    return t("table.locked");
  }
  return t("table.unlocked");
}


/**
 * Field configuration for AcademicYear form and view.
 * This configuration is used by both GenericForm and ViewDialog.
 */
export function getAcademicYearFieldConfig(
  t: TFunction<"academicYears">
): FieldConfig<AcademicYear>[] {
  return [
    {
      name: "yearName",
      type: "text",
      label: t("form.fields.yearName"),
      placeholder: t("form.placeholders.yearName"),
      required: true,
      validation: { required: true },
      maxLength: 100,
      viewFormat: viewStringOrDash,
    },

    makeNumberField(t, "totalCreditHours"),
    makeNumberField(t, "elementarySchoolHours"),
    makeNumberField(t, "middleSchoolHours"),

    makeDateTimeField(t, "budgetAnnouncementDate", true),
    makeDateTimeField(t, "allocationDeadline", false),

    {
      name: "isLocked",
      type: "checkbox",
      label: t("form.fields.isLocked"),
      description: t("form.fields.isLockedDescription"),
      required: false,
      viewFormat: (_value, data) => formatLockedStatus(t, data),
    },

    {
      name: "createdAt",
      type: "text",
      label: t("form.fields.createdAt"),
      required: false,
      showInForm: false,
      viewFormat: formatDateTimeForView,
    },

    {
      name: "updatedAt",
      type: "text",
      label: t("form.fields.updatedAt"),
      required: false,
      showInForm: false,
      viewFormat: formatDateTimeForView,
    },
  ];
}