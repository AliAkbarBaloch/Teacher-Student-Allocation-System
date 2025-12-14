import React, { useState, useEffect, useMemo, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { AlertCircle, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";
import type {
  GenericFormProps,
  FieldConfig,
  FieldRenderProps,
  SelectOption,
} from "./types/form.types";

/**
 * GenericForm - A configurable form component that renders fields based on configuration
 * 
 * This component handles:
 * - Form state management
 * - Field validation
 * - Form submission
 * - Different field types (text, number, select, checkbox, etc.)
 * 
 * @example
 * ```tsx
 * const fields: FieldConfig<AcademicYear>[] = [
 *   {
 *     name: 'yearName',
 *     type: 'text',
 *     label: 'Year Name',
 *     required: true,
 *     validation: { required: true }
 *   }
 * ];
 * 
 * <GenericForm
 *   fields={fields}
 *   onSubmit={handleSubmit}
 *   onCancel={handleCancel}
 * />
 * ```
 */
export function GenericForm<TData, TCreateRequest, TUpdateRequest>({
  fields,
  initialData,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
  mode = "create",
  translationNamespace,
  className,
}: GenericFormProps<TData, TCreateRequest, TUpdateRequest>) {
  const { t: tCommon } = useTranslation("common");
  const { t } = useTranslation(translationNamespace || "common");

  // Filter fields that should be shown in form mode
  const visibleFields = useMemo(
    () => fields.filter((field) => field.showInForm !== false),
    [fields]
  );

  // Initialize form data
  const [formData, setFormData] = useState<Partial<TData>>(() => {
    if (initialData) {
      return { ...initialData };
    }
    return {};
  });

  const [errors, setErrors] = useState<Partial<Record<keyof TData, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectOptionsCache, setSelectOptionsCache] = useState<
    Record<string, SelectOption[]>
  >({});
  const [loadingOptions, setLoadingOptions] = useState<Record<string, boolean>>({});

  // Update form data when initialData changes
  useEffect(() => {
    if (initialData) {
      const transformed: Partial<TData> = {};
      visibleFields.forEach((field) => {
        const value = initialData[field.name];
        const transformedValue = field.transform?.input
          ? field.transform.input(value)
          : value;
        // Type assertion is safe here because transform.input should return
        // a value compatible with the field's type
        transformed[field.name] = transformedValue as TData[keyof TData];
      });
      setFormData(transformed);
    } else {
      setFormData({});
    }
    setErrors({});
  }, [initialData, visibleFields]);

  // Load async select options - optimized to only load once per field
  useEffect(() => {
    const loadAsyncOptions = async () => {
      const fieldsToLoad = visibleFields.filter(
        (field) =>
          field.type === "select" &&
          typeof field.options === "function" &&
          !selectOptionsCache[String(field.name)] &&
          !loadingOptions[String(field.name)]
      );

      if (fieldsToLoad.length === 0) return;

      // Set loading state
      setLoadingOptions((prev) => {
        const newState = { ...prev };
        fieldsToLoad.forEach((field) => {
          newState[String(field.name)] = true;
        });
        return newState;
      });

      // Load all options in parallel
      const loadPromises = fieldsToLoad.map(async (field) => {
        try {
          const optionsLoader = field.options;
          if (typeof optionsLoader === "function") {
            const options = await optionsLoader();
            return { fieldName: String(field.name), options };
          }
          return { fieldName: String(field.name), options: [] };
        } catch (error) {
          console.error(`Failed to load options for ${String(field.name)}:`, error);
          return { fieldName: String(field.name), options: [] };
        }
      });

      const results = await Promise.all(loadPromises);

      // Update cache and loading state
      setSelectOptionsCache((prev) => {
        const newCache = { ...prev };
        results.forEach(({ fieldName, options }) => {
          newCache[fieldName] = options;
        });
        return newCache;
      });

      setLoadingOptions((prev) => {
        const newState = { ...prev };
        fieldsToLoad.forEach((field) => {
          delete newState[String(field.name)];
        });
        return newState;
      });
    };

    loadAsyncOptions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visibleFields]);

  // Validation function - not memoized because it needs to read latest formData
  // Called only on submit, so recreation is not a performance concern
  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof TData, string>> = {};

    visibleFields.forEach((field) => {
      const value = formData[field.name];
      const validation = field.validation;

      if (!validation) return;

      // Required validation
      if (validation.required || field.required) {
        // Check for empty values: undefined, null, empty string, 0 (for number fields), empty arrays
        const isEmpty =
          value === undefined ||
          value === null ||
          value === "" ||
          value === 0 ||
          (Array.isArray(value) && value.length === 0);
        
        if (isEmpty) {
          newErrors[field.name] = t(`form.errors.${String(field.name)}Required`) || 
            `${field.label} is required`;
          return;
        }
      }

      // Type-specific validations
      if (value !== undefined && value !== null && value !== "") {
        // String validations
        if (typeof value === "string") {
          if (validation.minLength && value.length < validation.minLength) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}MinLength`) ||
              `${field.label} must be at least ${validation.minLength} characters`;
            return;
          }
          if (validation.maxLength && value.length > validation.maxLength) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}MaxLength`) ||
              `${field.label} must be at most ${validation.maxLength} characters`;
            return;
          }
          if (field.maxLength && value.length > field.maxLength) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}MaxLength`) ||
              `${field.label} must be at most ${field.maxLength} characters`;
            return;
          }
          if (validation.pattern && !validation.pattern.test(value)) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}Invalid`) ||
              `${field.label} is invalid`;
            return;
          }
        }

        // Number validations
        if (typeof value === "number") {
          if (validation.min !== undefined && value < validation.min) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}Min`) ||
              `${field.label} must be at least ${validation.min}`;
            return;
          }
          if (validation.max !== undefined && value > validation.max) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}Max`) ||
              `${field.label} must be at most ${validation.max}`;
            return;
          }
          if (field.min !== undefined && value < field.min) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}Min`) ||
              `${field.label} must be at least ${field.min}`;
            return;
          }
          if (field.max !== undefined && value > field.max) {
            newErrors[field.name] =
              t(`form.errors.${String(field.name)}Max`) ||
              `${field.label} must be at most ${field.max}`;
            return;
          }
        }

        // Custom validation
        if (validation.custom) {
          const customError = validation.custom(value, formData);
          if (customError) {
            newErrors[field.name] = customError;
            return;
          }
        }
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      // Transform data using field transforms
      const transformedData: Record<string, unknown> = {};
      visibleFields.forEach((field) => {
        const value = formData[field.name];
        transformedData[String(field.name)] = field.transform?.output
          ? field.transform.output(value)
          : value;
      });

      await onSubmit(transformedData as TCreateRequest | TUpdateRequest);
    } catch (error) {
      // Error handling is done by parent component
      console.error("Form submission error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle field value change - memoized to prevent unnecessary re-renders
  const handleChange = useCallback((fieldName: keyof TData, value: unknown) => {
    setFormData((prev) => ({ ...prev, [fieldName]: value }));
    // Clear error for this field when user starts typing
    setErrors((prev) => {
      if (prev[fieldName]) {
        const newErrors = { ...prev };
        delete newErrors[fieldName];
        return newErrors;
      }
      return prev;
    });
  }, []);

  // Check if field is disabled - memoized
  const isFieldDisabled = useCallback(
    (field: FieldConfig<TData>): boolean => {
      if (isLoading || isSubmitting) return true;
      if (typeof field.disabled === "function") {
        return field.disabled(formData as TData);
      }
      return field.disabled || false;
    },
    [isLoading, isSubmitting, formData]
  );

  // Render a single field
  const renderField = (field: FieldConfig<TData>) => {
    const value = formData[field.name];
    const error = errors[field.name];
    const disabled = isFieldDisabled(field);
    const fieldId = `field-${String(field.name)}`;

    // Custom render function
    if (field.render) {
      const renderProps: FieldRenderProps<TData> = {
        value,
        onChange: (newValue) => handleChange(field.name, newValue),
        error: error as string | undefined,
        disabled,
        field,
        formData: formData as TData,
      };
      return (
        <div
          key={String(field.name)}
          className={`grid gap-1 ${field.colSpan === 2 ? "md:col-span-2" : "col-span-1"}`}
        >
          {field.render(renderProps)}
        </div>
      );
    }

    // Default rendering based on field type
    const commonProps = {
      id: fieldId,
      disabled,
      className: error ? "border-destructive" : "",
    };

    let inputElement: React.ReactNode;

    switch (field.type) {
      case "text":
      case "email":
      case "password":
        inputElement = (
          <Input
            {...commonProps}
            type={field.type}
            value={(value as string) || ""}
            onChange={(e) => handleChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            maxLength={field.maxLength}
          />
        );
        break;

      case "number":
        inputElement = (
          <Input
            {...commonProps}
            type="number"
            value={(value as number) ?? ""}
            onChange={(e) =>
              handleChange(
                field.name,
                e.target.value === "" ? undefined : Number(e.target.value)
              )
            }
            placeholder={field.placeholder}
            min={field.min}
            max={field.max}
            step={field.step}
          />
        );
        break;

      case "datetime-local":
      case "date":
      case "time":
        inputElement = (
          <Input
            {...commonProps}
            type={field.type}
            value={(value as string) || ""}
            onChange={(e) => handleChange(field.name, e.target.value || null)}
            placeholder={field.placeholder}
          />
        );
        break;

      case "textarea":
        inputElement = (
          <Textarea
            {...commonProps}
            value={(value as string) || ""}
            onChange={(e) => handleChange(field.name, e.target.value)}
            placeholder={field.placeholder}
            maxLength={field.maxLength}
            rows={field.rows || 3}
          />
        );
        break;

      case "select": {
        const selectOptions: SelectOption[] =
          typeof field.options === "function"
            ? selectOptionsCache[String(field.name)] || []
            : field.options || [];

        const isLoadingOptions =
          typeof field.options === "function" && loadingOptions[String(field.name)];

        // Normalize value for select - use "__none__" for empty (matches original form behavior)
        // The original form uses "__none__" as the value when empty, and shows it as a SelectItem
        // This allows SelectValue to display the SelectItem's label (which is the placeholder text)
        const isEmpty = value === null || value === undefined || value === "" || value === 0;
        const selectValue = isEmpty ? "__none__" : String(value);

        inputElement = (
          <Select
            value={selectValue}
            onValueChange={(newValue) => {
              if (newValue === "__none__") {
                // Match original form behavior: set to 0 for required fields, null for optional
                handleChange(field.name, field.required ? 0 : null);
              } else if (newValue) {
                // Try to preserve original type
                const option = selectOptions.find((opt) => String(opt.value) === newValue);
                handleChange(field.name, option?.value ?? newValue);
              }
            }}
            disabled={disabled || isLoadingOptions}
          >
            <SelectTrigger
              className={`w-full ${error ? "border-destructive" : ""}`}
            >
              <SelectValue placeholder={field.placeholder || "Select..."} />
            </SelectTrigger>
            <SelectContent>
              {/* Always show "__none__" option - it acts as placeholder and initial state */}
              {/* For required fields, users can select it but validation will prevent submission */}
              <SelectItem value="__none__">
                {field.placeholder || (field.required ? "Select..." : "None")}
              </SelectItem>
              {isLoadingOptions ? (
                // Show loading state for async options
                <SelectItem value="__none__" disabled>
                  {tCommon("actions.loading") || "Loading..."}
                </SelectItem>
              ) : selectOptions.length === 0 && typeof field.options === "function" ? (
                // Show empty state if no options loaded
                <SelectItem value="__none__" disabled>
                  {t("form.placeholders.noOptions") || "No options available"}
                </SelectItem>
              ) : (
                selectOptions.map((option) => (
                  <SelectItem key={String(option.value)} value={String(option.value)}>
                    {option.label}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
        );
        break;
      }

      case "checkbox":
        // Checkbox fields are rendered differently (full-width with label)
        return (
          <div
            key={String(field.name)}
            className={`grid gap-1 ${field.colSpan === 2 ? "md:col-span-2" : "col-span-1"}`}
          >
            <Label
              htmlFor={fieldId}
              className="hover:bg-accent/50 flex items-start gap-3 rounded-lg border p-4 cursor-pointer has-aria-checked:border-primary has-aria-checked:bg-primary/10 transition-colors"
            >
              <Checkbox
                id={fieldId}
                checked={!!value}
                onCheckedChange={(checked) =>
                  handleChange(field.name, checked === true)
                }
                disabled={disabled}
                className="h-5 w-5 mt-0.5 data-[state=checked]:border-primary data-[state=checked]:bg-primary"
              />
              <div className="grid gap-1.5 flex-1">
                <p className="text-sm font-medium leading-none">
                  {field.label}
                  {field.required && (
                    <span className="text-destructive ml-1">*</span>
                  )}
                </p>
                {field.description && (
                  <p className="text-xs text-muted-foreground">
                    {field.description}
                  </p>
                )}
                {field.type === "checkbox" && (
                  <p className="text-sm text-muted-foreground mt-1">
                    {value ? tCommon("actions.yes") || "Yes" : tCommon("actions.no") || "No"}
                  </p>
                )}
              </div>
            </Label>
            {error && <p className="text-sm text-destructive">{error}</p>}
          </div>
        );

      default:
        inputElement = (
          <Input
            {...commonProps}
            value={(value as string) || ""}
            onChange={(e) => handleChange(field.name, e.target.value)}
            placeholder={field.placeholder}
          />
        );
    }

    return (
      <div
        key={String(field.name)}
        className={`grid gap-1 ${field.colSpan === 2 ? "md:col-span-2" : "col-span-1"}`}
      >
        <Label htmlFor={fieldId} className="text-sm font-medium">
          {field.label}
          {(field.required || field.validation?.required) && (
            <span className="text-destructive ml-1">*</span>
          )}
        </Label>
        {inputElement}
        {field.description && (
          <p className="text-xs text-muted-foreground">{field.description}</p>
        )}
        {error && <p className="text-sm text-destructive">{error}</p>}
      </div>
    );
  };

  return (
    <form onSubmit={handleSubmit} className={cn("pb-2", className)}>
      {/* Error display */}
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <AlertCircle className="h-4 w-4" />
          <span>{externalError || (Object.values(errors)[0] as string)}</span>
        </div>
      )}

      {/* Form fields */}
      <div className="grid gap-4 md:grid-cols-2">
        {visibleFields.map((field) => renderField(field))}
      </div>

      {/* Form actions */}
      <div className="flex justify-end gap-2 pt-2">
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
          ) : mode === "edit" ? (
            tCommon("actions.update")
          ) : (
            tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}
