import { useCallback, useEffect, useMemo, useState } from "react";
import type { TFunction } from "i18next";
import { validateEmail, validatePhoneNumber } from "@/lib/validationUtils";
import { useAppStore } from "@/store";
import { EMPLOYMENT_STATUS_OPTIONS, USAGE_CYCLE_OPTIONS } from "@/lib/constants/teachers";
import type {
  ApiErrorResponse,
  CreateTeacherRequest,
  Teacher,
  TeacherFormErrors,
  EmploymentStatus,
  UsageCycle,
  UpdateTeacherRequest,
} from "../types/teacher.types";
import { isApiError } from "../types/teacher.types";

import type { Subject } from "@/features/subjects/types/subject.types";
import { SubjectService } from "@/features/subjects/services/subjectService";

type FormState = {
  schoolId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  isPartTime: boolean;
  workingHoursPerWeek: string;
  employmentStatus: EmploymentStatus | "";
  usageCycle: UsageCycle | "";
  subjectIds: number[];
};

const createDefaultState = (): FormState => ({
  schoolId: "",
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  isPartTime: false,
  workingHoursPerWeek: "",
  employmentStatus: "",
  usageCycle: "",
  subjectIds: [],

});

// When editing a teacher 
const mapTeacherToFormState = (teacher: Teacher): FormState => ({
  schoolId: teacher.schoolId ? String(teacher.schoolId) : "",
  firstName: teacher.firstName ?? "",
  lastName: teacher.lastName ?? "",
  email: teacher.email ?? "",
  phone: teacher.phone ?? "",
  isPartTime: Boolean(teacher.isPartTime), // Keep for form state, but derive from employmentStatus in payload
  workingHoursPerWeek: teacher.workingHoursPerWeek != null ? String(teacher.workingHoursPerWeek) : "", // <-- Add this line
  employmentStatus: teacher.employmentStatus ?? "",
  usageCycle: teacher.usageCycle ?? "",
  // .map - go through subjects and extract only ids as an array. 
  // ? - only do map if teacher.subject exists
  // ?? - nullish coalesing. if the left side is null, use the right side 
  subjectIds: teacher.subjects?.map((s) => s.id) ?? [],
});

type BaseTeacherFormOptions = {
  t: TFunction<"teachers">;
};

type CreateFormOptions = BaseTeacherFormOptions & {
  mode: "create";
  teacher?: null;
  onSubmit: (payload: CreateTeacherRequest) => Promise<void>;
};

type EditFormOptions = BaseTeacherFormOptions & {
  mode: "edit";
  teacher: Teacher;
  onSubmit: (payload: UpdateTeacherRequest) => Promise<void>;
};

export type UseTeacherFormOptions = CreateFormOptions | EditFormOptions;

export function useTeacherForm(options: UseTeacherFormOptions) {
  const { mode, t, onSubmit } = options;
  const teacher = mode === "edit" ? options.teacher : null;
  const isEditMode = mode === "edit";

  const initialState = useMemo<FormState>(() => {
    if (isEditMode && teacher) {
      return mapTeacherToFormState(teacher);
    }
    return createDefaultState();
  }, [isEditMode, teacher]);

  //
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [subjectsLoading, setSubjectsLoading] = useState(false);

  const [formState, setFormState] = useState<FormState>(initialState);
  const [errors, setErrors] = useState<TeacherFormErrors>({});
  const [generalError, setGeneralError] = useState<string | null>(null);
  const [internalSubmitting, setInternalSubmitting] = useState(false);
  const schools = useAppStore((state) => state.schools);
  const loadingSchools = useAppStore((state) => state.schoolsLoading);
  const refreshSchools = useAppStore((state) => state.refreshSchools);

  useEffect(() => {
    if (isEditMode && teacher) {
      setFormState(mapTeacherToFormState(teacher));
    } else {
      setFormState(createDefaultState());
    }
    setErrors({});
    setGeneralError(null);
  }, [isEditMode, teacher]);

  // Refresh schools when mode changes to ensure fresh data
  useEffect(() => {
    if (mode === "create") {
      refreshSchools();
    }
  }, [mode, refreshSchools]);

  //run once 
  useEffect(() => {

    //assume the component is currently on the screen 
    let mounted = true; 

    //
    async function loadSubjects(){

      setSubjectsLoading(true);
      try{
        //wait until backend sends me all subjects, store result in res 
        const res = await SubjectService.getAll();
        //only update state if this screen is still open 
        if (mounted) setSubjects(res);
      } catch (e) {
        console.error("Failed to load subjects", e);
        if (mounted) setSubjects([]);
      } finally {
        //loading is done 
        if (mounted) setSubjectsLoading(false);
      }
    }

    //start the loading 
    loadSubjects();

    //called when the component is removed from the screen 
    return () => {

      mounted = false; 
      
    };

  }, []);

  const employmentStatusOptions = useMemo(() => {
    return EMPLOYMENT_STATUS_OPTIONS.map((status) => ({
      value: status,
      label: t(`form.employmentStatus.${status}`, { defaultValue: status }),
    }));
  }, [t]);

  const usageCycleOptions = useMemo(() => {
    return USAGE_CYCLE_OPTIONS.map((cycle) => ({
      value: cycle,
      label: t(`form.usageCycle.${cycle}`, { defaultValue: cycle }),
    }));
  }, [t]);

  const resetFieldError = useCallback((field: keyof TeacherFormErrors) => {
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  }, [errors]);
  // when a form changes, update the form state 
  const handleInputChange = useCallback(
    (field: keyof FormState, value: string | boolean | number | number[]) => {
      let newValue = value;
      if (field === "workingHoursPerWeek" && typeof value === "number") {
        newValue = String(value);
      }
      setFormState((prev) => ({ ...prev, [field]: newValue }));
      resetFieldError(field as keyof TeacherFormErrors);
    },
    [resetFieldError]
  );

  const validate = useCallback((): boolean => {
    const validationErrors: TeacherFormErrors = {};

    if (!formState.schoolId.trim()) {
      validationErrors.schoolId = t("form.errors.schoolIdRequired");
    } else if (Number.isNaN(Number(formState.schoolId)) || Number(formState.schoolId) <= 0) {
      validationErrors.schoolId = t("form.errors.schoolIdInvalid");
    }

    if (!formState.firstName.trim()) {
      validationErrors.firstName = t("form.errors.firstNameRequired");
    } else if (formState.firstName.trim().length < 2 || formState.firstName.trim().length > 100) {
      validationErrors.firstName = t("form.errors.firstNameLength");
    }

    if (!formState.lastName.trim()) {
      validationErrors.lastName = t("form.errors.lastNameRequired");
    } else if (formState.lastName.trim().length < 2 || formState.lastName.trim().length > 100) {
      validationErrors.lastName = t("form.errors.lastNameLength");
    }

    if (!formState.email.trim()) {
      validationErrors.email = t("form.errors.emailRequired");
    } else if (!validateEmail(formState.email.trim())) {
      validationErrors.email = t("form.errors.emailInvalid");
    }

    if (formState.phone.trim() && !validatePhoneNumber(formState.phone.trim())) {
      validationErrors.phone = t("form.errors.phoneInvalid");
    }

    if (!formState.employmentStatus) {
      validationErrors.employmentStatus = t("form.errors.employmentStatusRequired");
    }

    if (formState.isPartTime) {
      if (!formState.workingHoursPerWeek.trim()) {
        validationErrors.workingHoursPerWeek = t("form.errors.workingHoursRequired");
      } else if (
        isNaN(Number(formState.workingHoursPerWeek)) ||
        Number(formState.workingHoursPerWeek) <= 0
      ) {
        validationErrors.workingHoursPerWeek = t("form.errors.workingHoursInvalid");
      }
    }

    setErrors(validationErrors);
    return Object.keys(validationErrors).length === 0;
  }, [formState, t]);

  const buildBasePayload = useCallback((): CreateTeacherRequest => {
    const toOptionalString = (value: string) => {
      const trimmed = value.trim();
      return trimmed ? trimmed : undefined;
    };
    const employmentStatus = formState.employmentStatus as EmploymentStatus;

    //Payload is sent to backend 
    const payload: CreateTeacherRequest = {
      schoolId: Number(formState.schoolId),
      firstName: formState.firstName.trim(),
      lastName: formState.lastName.trim(),
      email: formState.email.trim(),
      isPartTime: formState.isPartTime,
      workingHoursPerWeek: formState.isPartTime && String(formState.workingHoursPerWeek).trim() ? Number(formState.workingHoursPerWeek) : null,
      employmentStatus,
      subjectIds: formState.subjectIds,
    };

    // Only include optional fields if they have values
    const phone = toOptionalString(formState.phone);
    if (phone) {
      payload.phone = phone;
    }

    if (formState.usageCycle) {
      payload.usageCycle = formState.usageCycle as UsageCycle;
    }

    // Note: isActive is not part of the backend create DTO
    // Teachers are created as active by default in the backend
    // We can set it after creation if needed, but for now we'll omit it

    return payload;
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
          const updatePayload: UpdateTeacherRequest = {
            schoolId: basePayload.schoolId,
            firstName: basePayload.firstName,
            lastName: basePayload.lastName,
            email: basePayload.email,
            phone: basePayload.phone,
            isPartTime: basePayload.isPartTime,
            employmentStatus: basePayload.employmentStatus,
            usageCycle: basePayload.usageCycle,
            workingHoursPerWeek: basePayload.workingHoursPerWeek,
            subjectIds: basePayload.subjectIds,
          };
          await onSubmit(updatePayload);
        } else {
          await onSubmit(basePayload);
        }
        setErrors({});
      } catch (error) {
        if (isApiError(error)) {
          const apiError = error as ApiErrorResponse;
          if (apiError.details && typeof apiError.details === "object") {
            const detailEntries = Object.entries(apiError.details as Record<string, unknown>).reduce<TeacherFormErrors>(
              (acc, [field, message]) => {
                acc[field as keyof TeacherFormErrors] =
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
          setGeneralError(apiError.message || t("form.errors.submit"));
        } else if (error instanceof Error) {
          setGeneralError(error.message);
        } else {
          setGeneralError(t("form.errors.submit"));
        }
      } finally {
        setInternalSubmitting(false);
      }
    },
    [buildBasePayload, isEditMode, onSubmit, t, validate]
  );

  return {
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
    isEditMode,
    subjects,
    subjectsLoading,
  };
}

