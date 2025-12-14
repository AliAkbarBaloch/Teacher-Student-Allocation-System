// components
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { SchoolLocationMap } from "./SchoolLocationMap";

// types
import type { CreateSchoolRequest, School, UpdateSchoolRequest } from "../types/school.types";

// hooks
import { useSchoolForm } from "../hooks/useSchoolForm";

// translations
import { useTranslation } from "react-i18next";

// icons
import { NumberField } from "@/components/form/fields/NumberField";
import { SelectField } from "@/components/form/fields/SelectField";
import { TextAreaField } from "@/components/form/fields/TextAreaField";
import { TextField } from "@/components/form/fields/TextField";
import { AlertCircle, Loader2 } from "lucide-react";

type BaseSchoolFormProps = {
  onCancel: () => void;
  isSubmitting?: boolean;
};

type CreateSchoolFormProps = BaseSchoolFormProps & {
  mode: "create";
  school?: null;
  onSubmit: (values: CreateSchoolRequest) => Promise<void>;
};

type EditSchoolFormProps = BaseSchoolFormProps & {
  mode: "edit";
  school: School;
  onSubmit: (values: UpdateSchoolRequest) => Promise<void>;
};

type SchoolFormProps = CreateSchoolFormProps | EditSchoolFormProps;

export function SchoolForm(props: SchoolFormProps) {
  const { mode, onSubmit, onCancel, isSubmitting = false } = props;
  const { t } = useTranslation("schools");
  const { t: tCommon } = useTranslation("common");
  const hookProps =
    mode === "edit"
      ? { mode, onSubmit, school: props.school, t }
      : ({ mode, onSubmit, t } as const);

  const { formState, errors, generalError, typeOptions, handleInputChange, handleSubmit, internalSubmitting } =
    useSchoolForm(hookProps);

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {(generalError || Object.values(errors).some(Boolean)) && (
        <div className="flex items-center gap-2 rounded-md border border-destructive/20 bg-destructive/10 p-3 text-sm text-destructive">
          <AlertCircle className="h-4 w-4" />
          <span>{generalError || Object.values(errors).find(Boolean)}</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <TextField
          id="schoolName"
          label={t("form.fields.schoolName")}
          value={formState.schoolName}
          onChange={val => handleInputChange("schoolName", val)}
          placeholder={t("form.placeholders.schoolName")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.schoolName}
        />

        <SelectField
          id="schoolType"
          label={t("form.fields.schoolType")}
          value={formState.schoolType}
          onChange={val => handleInputChange("schoolType", val)}
          options={typeOptions}
          placeholder={t("form.placeholders.schoolType")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.schoolType}
        />

        <NumberField
          id="zoneNumber"
          label={t("form.fields.zoneNumber")}
          value={formState.zoneNumber}
          onChange={val => handleInputChange("zoneNumber", val)}
          placeholder={t("form.placeholders.zoneNumber")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.zoneNumber}
          min={1}
        />

        <NumberField
          id="latitude"
          label={t("form.fields.latitude")}
          value={formState.latitude}
          onChange={val => handleInputChange("latitude", val)}
          placeholder={t("form.placeholders.latitude")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.latitude}
        />

        <NumberField
          id="longitude"
          label={t("form.fields.longitude")}
          value={formState.longitude}
          onChange={val => handleInputChange("longitude", val)}
          placeholder={t("form.placeholders.longitude")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.longitude}
        />

        <NumberField
          id="distanceFromCenter"
          label={t("form.fields.distanceFromCenter")}
          value={formState.distanceFromCenter}
          onChange={val => handleInputChange("distanceFromCenter", val)}
          placeholder={t("form.placeholders.distanceFromCenter")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.distanceFromCenter}
          min={0}
        />
        {mode === "create" && (
          <p className="text-xs text-muted-foreground">
            Distance is automatically calculated from University of Passau. You can edit this value if needed.
          </p>
        )}

        <SelectField
          id="transportAccessibility"
          label={t("form.fields.transportAccessibility")}
          value={formState.transportAccessibility}
          onChange={val => handleInputChange("transportAccessibility", val)}
          options={[
            { value: "4a", label: "4a" },
            { value: "4b", label: "4b" },
          ]}
          placeholder={t("form.placeholders.transportAccessibility")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.transportAccessibility}
        />

        <TextField
          id="contactEmail"
          label={t("form.fields.contactEmail")}
          value={formState.contactEmail}
          onChange={val => handleInputChange("contactEmail", val)}
          placeholder={t("form.placeholders.contactEmail")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.contactEmail}
        />

        <TextField
          id="contactPhone"
          label={t("form.fields.contactPhone")}
          value={formState.contactPhone}
          onChange={val => handleInputChange("contactPhone", val)}
          placeholder={t("form.placeholders.contactPhone")}
          disabled={isSubmitting || internalSubmitting}
          error={errors.contactPhone}
        />
      </div>
      
      <TextAreaField
        id="address"
        label={t("form.fields.address")}
        value={formState.address}
        onChange={val => handleInputChange("address", val)}
        placeholder={t("form.placeholders.addressDetailed")}
        disabled={isSubmitting || internalSubmitting}
        error={errors.address}
      />

      {/* Location Map - shown in all modes */}
      {(formState.latitude || formState.longitude) && (
        <div className="space-y-2">
          <Label>Location Map</Label>
          <SchoolLocationMap
            latitude={formState.latitude}
            longitude={formState.longitude}
            schoolName={formState.schoolName || "School Location"}
            className="w-full"
          />
          <p className="text-xs text-muted-foreground">
            The map shows the school location and University of Passau for reference
          </p>
        </div>
      )}

      {mode === "create" && (
        <Label
          htmlFor="isActive"
          className="flex items-center gap-2 rounded-md border border-muted px-4 py-3 cursor-pointer hover:bg-accent/50 transition-colors has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10"
        >
          <Checkbox
            id="isActive"
            checked={formState.isActive}
            onCheckedChange={(checked) => handleInputChange("isActive", Boolean(checked))}
            disabled={isSubmitting || internalSubmitting}
            className="h-5 w-5"
          />
          <div>
            <span className="font-medium">
              {t("form.fields.isActive")}
            </span>
            <p className="text-xs text-muted-foreground">{t("form.helpers.isActive")}</p>
          </div>
        </Label>
      )}

      <div className="flex justify-end gap-2">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting || internalSubmitting}
        >
          {tCommon("actions.cancel")}
        </Button>
        <Button type="submit" disabled={isSubmitting || internalSubmitting}>
          {isSubmitting || internalSubmitting ? (
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

