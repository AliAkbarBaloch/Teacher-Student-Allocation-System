import type { TFunction } from "i18next";
import { useCallback, useEffect, useMemo, useState } from "react";

import { DISTANCE_DECIMAL_PLACES } from "@/lib/constants/app";
import { calculateDistanceFromUniversity } from "@/lib/utils/geoUtils";
import type {
  ApiErrorResponse,
  CreateSchoolRequest,
  School,
  SchoolFormErrors,
  SchoolType,
  UpdateSchoolRequest,
} from "../types/school.types";
import { isApiError } from "../types/school.types";
import { createSchoolTypeOptions } from "../utils/schoolOptions";

type FormState = {
  schoolName: string;
  schoolType: SchoolType | "";
  zoneNumber: string;
  address: string;
  latitude: string;
  longitude: string;
  distanceFromCenter: string;
  transportAccessibility: string;
  contactEmail: string;
  contactPhone: string;
  isActive: boolean;
};

const createDefaultState = (): FormState => ({
  schoolName: "",
  schoolType: "",
  zoneNumber: "",
  address: "",
  latitude: "",
  longitude: "",
  distanceFromCenter: "",
  transportAccessibility: "",
  contactEmail: "",
  contactPhone: "",
  isActive: true,
});

const mapSchoolToFormState = (school: School): FormState => ({
  schoolName: school.schoolName ?? "",
  schoolType: school.schoolType ?? "",
  zoneNumber: school.zoneNumber ? String(school.zoneNumber) : "",
  address: school.address ?? "",
  latitude: school.latitude !== null && typeof school.latitude !== "undefined" ? String(school.latitude) : "",
  longitude: school.longitude !== null && typeof school.longitude !== "undefined" ? String(school.longitude) : "",
  distanceFromCenter:
    school.distanceFromCenter !== null && typeof school.distanceFromCenter !== "undefined"
      ? String(school.distanceFromCenter)
      : "",
  transportAccessibility: school.transportAccessibility ?? "",
  contactEmail: school.contactEmail ?? "",
  contactPhone: school.contactPhone ?? "",
  isActive: Boolean(school.isActive),
});

type BaseSchoolFormOptions = {
  t: TFunction<"schools">;
};

type CreateFormOptions = BaseSchoolFormOptions & {
  mode: "create";
  school?: null;
  onSubmit: (payload: CreateSchoolRequest) => Promise<void>;
};

type EditFormOptions = BaseSchoolFormOptions & {
  mode: "edit";
  school: School;
  onSubmit: (payload: UpdateSchoolRequest) => Promise<void>;
};

export type UseSchoolFormOptions = CreateFormOptions | EditFormOptions;

export function useSchoolForm(options: UseSchoolFormOptions) {
  const { mode, t, onSubmit } = options;
  const school = mode === "edit" ? options.school : null;
  const isEditMode = mode === "edit";

  const initialState = useMemo<FormState>(() => {
    if (isEditMode && school) {
      return mapSchoolToFormState(school);
    }
    return createDefaultState();
  }, [isEditMode, school]);

  const [formState, setFormState] = useState<FormState>(initialState);
  const [errors, setErrors] = useState<SchoolFormErrors>({});
  const [generalError, setGeneralError] = useState<string | null>(null);
  const [internalSubmitting, setInternalSubmitting] = useState(false);
  const [distanceAutoCalculated, setDistanceAutoCalculated] = useState(false);

  useEffect(() => {
    setFormState(initialState);
    setErrors({});
    setGeneralError(null);
  }, [initialState]);

  // Auto-calculate distance from center when latitude or longitude changes
  useEffect(() => {
    // Only auto-calculate in create mode or if distance is empty in edit mode
    const currentDistance = formState.distanceFromCenter;
    if (isEditMode && currentDistance) {
      return; // Don't override existing distance in edit mode
    }

    const distance = calculateDistanceFromUniversity(formState.latitude, formState.longitude);
    if (distance !== null) {
      const newDistance = distance.toFixed(DISTANCE_DECIMAL_PLACES);
      // Only update if the value actually changed to avoid infinite loops
      if (currentDistance !== newDistance) {
        setFormState((prev) => ({
          ...prev,
          distanceFromCenter: newDistance,
        }));
        setDistanceAutoCalculated(true);
        // Clear the indicator after 3 seconds
        setTimeout(() => setDistanceAutoCalculated(false), 3000);
      }
    } else if (formState.latitude === "" && formState.longitude === "" && currentDistance !== "") {
      // Clear distance if both coordinates are empty
      setFormState((prev) => ({
        ...prev,
        distanceFromCenter: "",
      }));
      setDistanceAutoCalculated(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [formState.latitude, formState.longitude, isEditMode]);

  const baseTypeOptions = useMemo(() => createSchoolTypeOptions(t), [t]);

  const typeOptions = useMemo(() => {
    if (isEditMode && school) {
      const exists = baseTypeOptions.some((option) => option.value === school.schoolType);
      if (!exists && school.schoolType) {
        return [
          ...baseTypeOptions,
          {
            value: school.schoolType as SchoolType,
            label: t(`typeLabels.${school.schoolType}`, { defaultValue: school.schoolType }),
          },
        ];
      }
    }
    return baseTypeOptions;
  }, [baseTypeOptions, isEditMode, school, t]);

  const resetFieldError = useCallback((field: keyof SchoolFormErrors) => {
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  }, [errors]);

  const handleInputChange = useCallback(
    (field: keyof FormState, value: string | boolean | number) => {
      setFormState((prev) => ({ ...prev, [field]: value }));
      resetFieldError(field as keyof SchoolFormErrors);
    },
    [resetFieldError]
  );

  const validate = useCallback((): boolean => {
    const validationErrors: SchoolFormErrors = {};

    if (!formState.schoolName.trim()) {
      validationErrors.schoolName = t("form.errors.schoolNameRequired");
    }

    if (!formState.schoolType) {
      validationErrors.schoolType = t("form.errors.schoolTypeRequired");
    }

    if (!formState.zoneNumber.trim()) {
      validationErrors.zoneNumber = t("form.errors.zoneNumberRequired");
    } else if (Number.isNaN(Number(formState.zoneNumber)) || Number(formState.zoneNumber) <= 0) {
      validationErrors.zoneNumber = t("form.errors.zoneMustBePositive");
    }

    if (formState.latitude.trim() && Number.isNaN(Number(formState.latitude))) {
      validationErrors.latitude = t("form.errors.latitudeNumeric");
    }

    if (formState.longitude.trim() && Number.isNaN(Number(formState.longitude))) {
      validationErrors.longitude = t("form.errors.longitudeNumeric");
    }

    if (formState.distanceFromCenter.trim() && Number.isNaN(Number(formState.distanceFromCenter))) {
      validationErrors.distanceFromCenter = t("form.errors.distanceNumeric");
    }

    if (formState.contactEmail.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formState.contactEmail.trim())) {
        validationErrors.contactEmail = t("form.errors.emailInvalid");
      }
    }

    setErrors(validationErrors);
    return Object.keys(validationErrors).length === 0;
  }, [formState, t]);

  const buildBasePayload = useCallback((): CreateSchoolRequest => {
    const toNullableNumber = (value: string) => (value.trim() ? Number(value) : null);
    const toOptionalString = (value: string) => value.trim() || undefined;

    return {
      schoolName: formState.schoolName.trim(),
      schoolType: formState.schoolType as SchoolType,
      zoneNumber: Number(formState.zoneNumber),
      address: toOptionalString(formState.address),
      latitude: toNullableNumber(formState.latitude),
      longitude: toNullableNumber(formState.longitude),
      distanceFromCenter: toNullableNumber(formState.distanceFromCenter),
      transportAccessibility: toOptionalString(formState.transportAccessibility),
      contactEmail: toOptionalString(formState.contactEmail),
      contactPhone: toOptionalString(formState.contactPhone),
      isActive: formState.isActive,
    };
  }, [formState]);

  const handleSubmit = useCallback(
    async (event: React.FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      setGeneralError(null);

      if (!validate()) {
        return;
      }

      setInternalSubmitting(true);
      try {
        const basePayload = buildBasePayload();

        if (isEditMode) {
          const updatePayload: UpdateSchoolRequest = { ...basePayload };
          delete updatePayload.isActive;
          await onSubmit(updatePayload);
        } else {
          await onSubmit(basePayload);
        }
        setErrors({});
      } catch (error) {
        if (isApiError(error)) {
          const apiError = error as ApiErrorResponse;
          if (apiError.details && typeof apiError.details === "object") {
            const detailEntries = Object.entries(apiError.details as Record<string, unknown>).reduce<SchoolFormErrors>(
              (acc, [field, message]) => {
                acc[field as keyof SchoolFormErrors] =
                  typeof message === "string" ? message : String(message ?? "");
                return acc;
              },
              {}
            );
            setErrors((prev) => ({
              ...prev,
              ...detailEntries,
            }));
          }
          setGeneralError(apiError.message || t("errors.submit"));
        } else if (error instanceof Error) {
          setGeneralError(error.message);
        } else {
          setGeneralError(t("errors.submit"));
        }
      } finally {
        setInternalSubmitting(false);
      }
    },
    [buildBasePayload, isEditMode, onSubmit, t, validate]
  );

  // Reset auto-calculated indicator when user manually changes distance
  const handleDistanceChange = useCallback((value: string) => {
    setDistanceAutoCalculated(false);
    handleInputChange("distanceFromCenter", value);
  }, [handleInputChange]);

  return {
    formState,
    errors,
    generalError,
    typeOptions,
    handleInputChange,
    handleDistanceChange,
    handleSubmit,
    internalSubmitting,
    isEditMode,
    distanceAutoCalculated,
  };
}

