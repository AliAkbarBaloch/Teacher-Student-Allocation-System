import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AlertCircle, Loader2 } from "lucide-react";
import type { Role, CreateRoleRequest, UpdateRoleRequest } from "../types/role.types";

interface RoleFormProps {
  role?: Role | null;
  onSubmit: (data: CreateRoleRequest | UpdateRoleRequest) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  error?: string | null;
}

export function RoleForm({
  role,
  onSubmit,
  onCancel,
  isLoading = false,
  error: externalError = null,
}: RoleFormProps) {
  const { t } = useTranslation("roles");
  const { t: tCommon } = useTranslation("common");
  const [formData, setFormData] = useState<CreateRoleRequest>({
    title: "",
    description: "",
  });
  const [errors, setErrors] = useState<Partial<Record<keyof CreateRoleRequest, string>>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (role) {
      setFormData({
        title: role.title,
        description: role.description,
      });
    } else {
      setFormData({
        title: "",
        description: "",
      });
    }
    setErrors({});
  }, [role]);

  const validate = (): boolean => {
    const newErrors: Partial<Record<keyof CreateRoleRequest, string>> = {};

    if (!formData.title.trim()) {
      newErrors.title = t("form.errors.titleRequired");
    } else if (formData.title.length > 255) {
      newErrors.title = t("form.errors.titleMaxLength");
    }

    if (!formData.description.trim()) {
      newErrors.description = t("form.errors.descriptionRequired");
    } else if (formData.description.length > 1000) {
      newErrors.description = t("form.errors.descriptionMaxLength");
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(formData);
    } catch {
      // Error handling is done by parent component
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof CreateRoleRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear error for this field when user starts typing
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {(externalError || Object.keys(errors).length > 0) && (
        <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
          <AlertCircle className="h-4 w-4" />
          <span>{externalError || Object.values(errors)[0]}</span>
        </div>
      )}

      <div className="space-y-2">
        <Label htmlFor="title">
          {t("form.fields.title")}
          <span className="text-destructive ml-1">*</span>
        </Label>
        <Input
          id="title"
          value={formData.title}
          onChange={(e) => handleChange("title", e.target.value)}
          placeholder={t("form.placeholders.title")}
          disabled={isLoading || isSubmitting}
          className={errors.title ? "border-destructive" : ""}
          maxLength={255}
        />
        {errors.title && (
          <p className="text-sm text-destructive">{errors.title}</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">
          {t("form.fields.description")}
          <span className="text-destructive ml-1">*</span>
        </Label>
        <textarea
          id="description"
          value={formData.description}
          onChange={(e) => handleChange("description", e.target.value)}
          placeholder={t("form.placeholders.description")}
          disabled={isLoading || isSubmitting}
          className={`flex min-h-[100px] w-full rounded-md border ${
            errors.description ? "border-destructive" : "border-input"
          } bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50`}
          maxLength={1000}
          rows={4}
        />
        <div className="flex justify-between items-center">
          {errors.description && (
            <p className="text-sm text-destructive">{errors.description}</p>
          )}
          <p className="text-xs text-muted-foreground ml-auto">
            {formData.description.length}/1000
          </p>
        </div>
      </div>

      <div className="flex justify-end gap-2 pt-4">
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
          ) : (
            role ? tCommon("actions.update") : tCommon("actions.create")
          )}
        </Button>
      </div>
    </form>
  );
}

