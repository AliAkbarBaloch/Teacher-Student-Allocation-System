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
import { SchoolLocationMap } from "./SchoolLocationMap";

// types
import type { CreateSchoolRequest, School, SchoolType, UpdateSchoolRequest } from "../types/school.types";

// hooks
import { useSchoolForm } from "../hooks/useSchoolForm";

// translations
import { useTranslation } from "react-i18next";

// icons
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
        <div className="space-y-2">
          <Label htmlFor="schoolName">
            {t("form.fields.schoolName")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="schoolName"
            value={formState.schoolName}
            onChange={(event) => handleInputChange("schoolName", event.target.value)}
            placeholder={t("form.placeholders.schoolName")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.schoolName ? "border-destructive" : ""}
          />
          {errors.schoolName && <p className="text-sm text-destructive">{errors.schoolName}</p>}
        </div>

        <div className="space-y-2">
          <Label>
            {t("form.fields.schoolType")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={formState.schoolType}
            onValueChange={(value) => handleInputChange("schoolType", value as SchoolType)}
            disabled={isSubmitting || internalSubmitting}
          >
            <SelectTrigger className={errors.schoolType ? "border-destructive" : ""}>
              <SelectValue placeholder={t("form.placeholders.schoolType")} />
            </SelectTrigger>
            <SelectContent>
              {typeOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.schoolType && <p className="text-sm text-destructive">{errors.schoolType}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor="zoneNumber">
            {t("form.fields.zoneNumber")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="zoneNumber"
            type="number"
            min={1}
            step={1}
            value={formState.zoneNumber}
            onChange={(event) => handleInputChange("zoneNumber", event.target.value)}
            placeholder={t("form.placeholders.zoneNumber")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.zoneNumber ? "border-destructive" : ""}
          />
          {errors.zoneNumber && <p className="text-sm text-destructive">{errors.zoneNumber}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor="latitude">{t("form.fields.latitude")}</Label>
          <Input
            id="latitude"
            type="number"
            step="0.000001"
            value={formState.latitude}
            onChange={(event) => handleInputChange("latitude", event.target.value)}
            placeholder={t("form.placeholders.latitude")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.latitude ? "border-destructive" : ""}
          />
          {errors.latitude && <p className="text-sm text-destructive">{errors.latitude}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor="longitude">{t("form.fields.longitude")}</Label>
          <Input
            id="longitude"
            type="number"
            step="0.000001"
            value={formState.longitude}
            onChange={(event) => handleInputChange("longitude", event.target.value)}
            placeholder={t("form.placeholders.longitude")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.longitude ? "border-destructive" : ""}
          />
          {errors.longitude && <p className="text-sm text-destructive">{errors.longitude}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor="distanceFromCenter">{t("form.fields.distanceFromCenter")}</Label>
          <Input
            id="distanceFromCenter"
            type="number"
            step="0.1"
            value={formState.distanceFromCenter}
            onChange={(event) => handleInputChange("distanceFromCenter", event.target.value)}
            placeholder={t("form.placeholders.distanceFromCenter")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.distanceFromCenter ? "border-destructive" : ""}
          />
          {mode === "create" && (
            <p className="text-xs text-muted-foreground">
              Distance is automatically calculated from University of Passau. You can edit this value if needed.
            </p>
          )}
          {errors.distanceFromCenter && (
            <p className="text-sm text-destructive">{errors.distanceFromCenter}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="transportAccessibility">{t("form.fields.transportAccessibility")}</Label>
          <Input
            id="transportAccessibility"
            value={formState.transportAccessibility}
            onChange={(event) => handleInputChange("transportAccessibility", event.target.value)}
            placeholder={t("form.placeholders.transportAccessibility")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.transportAccessibility ? "border-destructive" : ""}
          />
          {errors.transportAccessibility && (
            <p className="text-sm text-destructive">{errors.transportAccessibility}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="contactEmail">{t("form.fields.contactEmail")}</Label>
          <Input
            id="contactEmail"
            type="email"
            value={formState.contactEmail}
            onChange={(event) => handleInputChange("contactEmail", event.target.value)}
            placeholder={t("form.placeholders.contactEmail")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.contactEmail ? "border-destructive" : ""}
          />
          {errors.contactEmail && <p className="text-sm text-destructive">{errors.contactEmail}</p>}
        </div>

        <div className="space-y-2">
          <Label htmlFor="contactPhone">{t("form.fields.contactPhone")}</Label>
          <Input
            id="contactPhone"
            value={formState.contactPhone}
            onChange={(event) => handleInputChange("contactPhone", event.target.value)}
            placeholder={t("form.placeholders.contactPhone")}
            disabled={isSubmitting || internalSubmitting}
            className={errors.contactPhone ? "border-destructive" : ""}
          />
          {errors.contactPhone && <p className="text-sm text-destructive">{errors.contactPhone}</p>}
        </div>
      </div>

      <div className="space-y-2">
        <Label htmlFor="address">{t("form.fields.address")}</Label>
        <textarea
          id="address"
          rows={3}
          value={formState.address}
          placeholder={t("form.placeholders.addressDetailed")}
          onChange={(event) => handleInputChange("address", event.target.value)}
          disabled={isSubmitting || internalSubmitting}
          className={`flex min-h-[90px] w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
            errors.address ? "border-destructive" : "border-input"
          }`}
        />
        {errors.address && <p className="text-sm text-destructive">{errors.address}</p>}
      </div>

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
        <div className="flex items-center gap-2 rounded-md border border-muted px-4 py-3">
          <Checkbox
            id="isActive"
            checked={formState.isActive}
            onCheckedChange={(checked) => handleInputChange("isActive", Boolean(checked))}
            disabled={isSubmitting || internalSubmitting}
          />
          <div>
            <Label htmlFor="isActive" className="font-medium">
              {t("form.fields.isActive")}
            </Label>
            <p className="text-xs text-muted-foreground">{t("form.helpers.isActive")}</p>
          </div>
        </div>
      )}

      <div className="flex justify-end gap-2">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting || internalSubmitting}
        >
          {t("form.actions.cancel")}
        </Button>
        <Button type="submit" disabled={isSubmitting || internalSubmitting}>
          {isSubmitting || internalSubmitting ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              {t("form.actions.saving")}
            </>
          ) : mode === "edit" ? (
            t("form.actions.update")
          ) : (
            t("form.actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}

