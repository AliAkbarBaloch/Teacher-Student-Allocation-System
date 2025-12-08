import { useState, useEffect, useMemo } from "react";
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
import type {
  ZoneConstraint,
  CreateZoneConstraintRequest,
  UpdateZoneConstraintRequest,
} from "../types/zoneConstraint.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";
import { InternshipTypeService } from "@/features/internship-types/services/internshipTypeService";

interface ZoneConstraintFormProps {
  zoneConstraint?: ZoneConstraint | null;
  onSubmit: (data: CreateZoneConstraintRequest | UpdateZoneConstraintRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function ZoneConstraintForm({
  zoneConstraint,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: ZoneConstraintFormProps) {
  const { t } = useTranslation("zoneConstraints");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState<CreateZoneConstraintRequest>(() => {
    if (zoneConstraint) {
      return {
        zoneNumber: zoneConstraint.zoneNumber || 1,
        internshipTypeId: zoneConstraint.internshipTypeId || 0,
        isAllowed: zoneConstraint.isAllowed ?? true,
        description: zoneConstraint.description ?? "",
      };
    }
    return {
      zoneNumber: 1,
      internshipTypeId: 0,
      isAllowed: true,
      description: "",
    };
  });

  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loadingInternshipTypes, setLoadingInternshipTypes] = useState(true);
  const [errors, setErrors] = useState<Partial<Record<keyof CreateZoneConstraintRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load internship types
  useEffect(() => {
    const loadTypes = async () => {
      try {
        const data = await InternshipTypeService.getAll();
        setInternshipTypes(data);
      } catch (error) {
        console.log(error);
        setInternshipTypes([]);
      } finally {
        setLoadingInternshipTypes(false);
      }
    };
    loadTypes();
  }, []);

  useEffect(() => {
    if (zoneConstraint) {
      setFormData({
        zoneNumber: zoneConstraint.zoneNumber || 1,
        internshipTypeId: zoneConstraint.internshipTypeId || 0,
        isAllowed: zoneConstraint.isAllowed ?? true,
        description: zoneConstraint.description ?? "",
      });
    } else {
      setFormData({
        zoneNumber: 1,
        internshipTypeId: 0,
        isAllowed: true,
        description: "",
      });
    }
    setErrors({});
  }, [zoneConstraint]);

  // Normalize Select value
  const internshipTypeValue = useMemo((): string => {
    return formData.internshipTypeId > 0 ? String(formData.internshipTypeId) : "__none__";
  }, [formData.internshipTypeId]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateZoneConstraintRequest, string>> = {};

    if (!formData.zoneNumber || formData.zoneNumber < 1) {
      newErrors.zoneNumber = t("form.errors.zoneNumberRequired");
    }
    if (!formData.internshipTypeId || formData.internshipTypeId < 1) {
      newErrors.internshipTypeId = t("form.errors.internshipTypeIdRequired");
    }
    if (formData.description && formData.description.length > 1000) {
      newErrors.description = t("form.errors.descriptionMaxLength");
    }

    // Check if internshipTypeId exists in loaded types
    if (
      formData.internshipTypeId &&
      !internshipTypes.some((type) => type.id === formData.internshipTypeId)
    ) {
      newErrors.internshipTypeId = t("form.errors.internshipTypeIdRequired");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loadingInternshipTypes) return;
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      if (zoneConstraint) {
        const updateData: UpdateZoneConstraintRequest = {
          zoneNumber: formData.zoneNumber,
          internshipTypeId: formData.internshipTypeId,
          isAllowed: formData.isAllowed,
          description: formData.description,
        };
        await onSubmit(updateData);
      } else {
        const createData: CreateZoneConstraintRequest = {
          zoneNumber: formData.zoneNumber,
          internshipTypeId: formData.internshipTypeId,
          isAllowed: formData.isAllowed,
          description: formData.description,
        };
        await onSubmit(createData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateZoneConstraintRequest, value: string | number | boolean | null) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 py-4">
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <span>{externalError || Object.values(errors)[0]}</span>
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2 col-span-1">
          <Label htmlFor="zoneNumber" className="text-sm font-medium">
            {t("form.fields.zoneNumber")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Input
            id="zoneNumber"
            type="number"
            min={1}
            value={formData.zoneNumber}
            onChange={(e) => handleChange("zoneNumber", Number(e.target.value))}
            placeholder={t("form.placeholders.zoneNumber")}
            disabled={isLoading || isSubmitting}
            className={errors.zoneNumber ? "border-destructive" : ""}
          />
          {errors.zoneNumber && (
            <p className="text-sm text-destructive">{errors.zoneNumber}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <Label htmlFor="internshipTypeId" className="text-sm font-medium">
            {t("form.fields.internshipTypeId")}
            <span className="text-destructive ml-1">*</span>
          </Label>
          <Select
            value={internshipTypeValue}
            onValueChange={(value) => {
              if (value === "__none__") {
                handleChange("internshipTypeId", 0);
              } else {
                const id = parseInt(value, 10);
                handleChange("internshipTypeId", isNaN(id) ? 0 : id);
              }
            }}
            disabled={isLoading || isSubmitting || loadingInternshipTypes}
          >
            <SelectTrigger className={`w-full ${errors.internshipTypeId ? "border-destructive" : ""}`}>
              <SelectValue placeholder={t("form.placeholders.internshipTypeId")} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">
                {t("form.placeholders.internshipTypeId")}
              </SelectItem>
              {internshipTypes.length === 0 && !loadingInternshipTypes ? (
                <SelectItem value="__none__" disabled>
                  {t("form.placeholders.internshipTypeId")}
                </SelectItem>
              ) : (
                internshipTypes.map((type) => (
                  <SelectItem key={type.id} value={String(type.id)}>
                    {type.internshipCode} - {type.fullName}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
          {errors.internshipTypeId && (
            <p className="text-sm text-destructive">{errors.internshipTypeId}</p>
          )}
        </div>

        <div className="space-y-2 col-span-1">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="isAllowed"
              checked={!!formData.isAllowed}
              onCheckedChange={(checked) =>
                handleChange("isAllowed", checked === true)
              }
              disabled={isLoading || isSubmitting}
              className="h-5 w-5"
            />
            <Label
              htmlFor="isAllowed"
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
            >
              {t("form.fields.isAllowed")}
            </Label>
          </div>
        </div>

        <div className="space-y-2 col-span-2">
          <Label htmlFor="description" className="text-sm font-medium">
            {t("form.fields.description")}
          </Label>
          <Input
            id="description"
            value={formData.description ?? ""}
            onChange={(e) => handleChange("description", e.target.value)}
            placeholder={t("form.placeholders.description")}
            disabled={isLoading || isSubmitting}
            className={errors.description ? "border-destructive" : ""}
            maxLength={1000}
          />
          {errors.description && (
            <p className="text-sm text-destructive">{errors.description}</p>
          )}
        </div>
      </div>

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
          {isSubmitting || isLoading
            ? tCommon("actions.saving")
            : zoneConstraint
            ? tCommon("actions.update")
            : tCommon("actions.create")}
        </Button>
      </div>
    </form>
  );
}