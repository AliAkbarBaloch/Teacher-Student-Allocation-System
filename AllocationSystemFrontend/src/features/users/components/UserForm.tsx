import React, { useEffect, useMemo, useState } from "react";
//allows to show text in multiple languages 
import { useTranslation } from "react-i18next";
//icons 
import { AlertCircle, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label"

//
// typescript type for a Role object from backend 
import type { Role } from "@/features/roles/types/role.types";

//typescript types for user, cretae patload, update payload, role enum  
import type {
    CreateUserRequest,
    UpdateUserRequest,
    User,
    UserRole,
} from "@/features/users/types/user.types";

import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

// what form stores 
type FormData = {

    fullName: string;
    email: string;
    phoneNumber: string;
    roleId: string;
    enabled: boolean;
    //create only 
    password: string;
    confirmPassword: string;

};

//an object where each field may or may not have an error message 
type FormErrors = Partial<Record<keyof FormData, string>>;

//props. what the UserForm get from parent 

interface UserFormProps {
    user?: User | null; //If present -> edit mode, if null -> create mode 
    roles: Role[]; //Role list for dropdown
    onSubmit: (data: CreateUserRequest | UpdateUserRequest) => Promise<void>; //Function called when form is valid 
    onCancel: () => void; //close dialog 
    isLoading?: boolean; //Disable form when backend is busy 
    error?: string | null; //Big API error
    fieldErrors?: Partial<Record<keyof FormData, string>>; //API filed level errors 
}

//Constrains 

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LEN = 6;

//helper: convert role title -> backend enum 
// admin -> ADMIN 
function toUserRoleEnum(roleTitle: string): UserRole | null {

    //remove spaces, convert to uppercase 
    const normalized = roleTitle.trim().toUpperCase();

    //Ensure backend-safe value 
    if (normalized === "USER" || normalized === "ADMIN" || normalized === "MODERATOR") {

        return normalized as UserRole;
    }

    return null;
}

//the component 

export function UserForm({
    user,
    roles,
    onSubmit,
    onCancel,
    isLoading = false,
    error: externalError = null, // renames the prop error to externalError 
    fieldErrors,
}: UserFormProps) {

    // translation hooks 
    //users.json 
    const { t } = useTranslation("users");
    //common.json 
    const { t: tCommon } = useTranslation("common");

    //if user exist - editing, overwise - creating 
    const isEditMode = Boolean(user);

    //Form state. stores everything the user typed 
    const [formData, setFormData] = useState<FormData>({
        fullName: "",
        email: "",
        phoneNumber: "",
        roleId: "",
        enabled: true,
        password: "",
        confirmPassword: "",
    });

    //default role logic. if roles exist, pick the firs one 
    //useMemo avoids recalculating unless role chnages 
    const firstRoleId = useMemo(() => {
        return roles.length > 0 ? String(roles[0].id): "";
    }, [roles]);

    //stores validation errors.
    const [errors, setErrors] = useState<FormErrors>({});

    //true while submit request is running 
    const [isSubmitting, setIsSubmitting] = useState(false);


    //fill form when editting, reset when creating 
    //runs whenenver user or firstRoleId changes 
    useEffect(() => {
        //if edditing 
        if (user) {
            setFormData(
                {
                    fullName: user.fullName ?? "",
                    email: user.email ?? "",
                    phoneNumber: user.phoneNumber ?? "",
                    roleId: user.roleId !== null ? String(user.roleId) : firstRoleId,
                    enabled: Boolean(user.enabled),
                    password: "",
                    confirmPassword: "",
                }
            );
        // creating a user - reset everything to blank  
        } else {
            setFormData({
                fullName: "",
                email: "",
                phoneNumber: "",
                roleId: firstRoleId,
                enabled: true,
                password: "",
                confirmPassword: "",
            });
        }
        //clear errors 
        setErrors({});
    }, [user, firstRoleId]);

    // Merge backend field errors 
    useEffect(() => {
        if (fieldErrors && Object.keys(fieldErrors).length > 0) {
            setErrors((prev) => ({ ...prev, ...fieldErrors }));
        }
    }, [fieldErrors]);

    //helper to update one field 
    const setField = <K extends keyof FormData>(key: K, value: FormData[K]) => {

        //copy previous state, replace only one property
        setFormData((prev) => ({...prev, [key]: value}));
        if (errors[key]) {
            setErrors((prev) => ({ ...prev, [key]: undefined}));
        }
    }

    //validating. next stores all errors found 
    const validate = (): boolean => {

        const next: FormErrors = {};

        if (!formData.fullName.trim()) {
            next.fullName = t("form.errors.fullNameRequired");
        }

        if (!formData.email.trim()) {
            next.email = t("form.errors.emailRequired");
        } else if (!EMAIL_REGEX.test(formData.email.trim())) {
            next.email = t("form.errors.emailInvalid");
        }

        if (!formData.roleId) {
            next.roleId = t("form.errors.roleRequired");
        } else {
            const selectedRole = roles.find((r) => String(r.id) === formData.roleId);
            if (!selectedRole) {
                next.roleId = t("form.errors.roleInvalid");
            } else {
                const enumRole = toUserRoleEnum(selectedRole.title);
                if (!enumRole) {
                    next.roleId = t("form.errors.roleInvalid");
                }
            }
        }



        //Create mode : password required 
        if (!isEditMode) {
            if (!formData.password) {
                next.password = t("form.errors.passwordRequired");
            } else if (formData.password.length < MIN_PASSWORD_LEN) {
                next.password = t("form.errors.passwordMinLength", {
                    min: MIN_PASSWORD_LEN
                })
            }

            if (!formData.confirmPassword) {
                next.confirmPassword = t("form.errors.confirmPasswordRequired");
            } else if (formData.password !== formData.confirmPassword) {
                next.confirmPassword = t("form.errors.passwordDoNotMatch");
            }
        }

        //save errors 
        setErrors(next);

        return (Object.keys(next).length === 0);

    }

    //build role payload 
    const buildRolePayload = () => {

        const selectedRole = roles.find((r) => String(r.id) === formData.roleId);
        if (!selectedRole) throw new Error("Invalid role selection");

        const enumRole = toUserRoleEnum(selectedRole.title);
        if (!enumRole) throw new Error("Selected role is not a valid enum role");

        return {
            roleId: Number(formData.roleId),
            role: enumRole,
        };
    };

    const handleSubmit = async (e: React.FormEvent) => {

        //stop browser from refreshing page 
        e.preventDefault();

        //form validation 
        if (!validate()) {
            return;
        }

        //show loading spinner, disable form 
        setIsSubmitting(true);

        try {
            const { roleId, role } = buildRolePayload();

            const normalizedEmail = formData.email.trim().toLocaleLowerCase();
            const normalizedName = formData.fullName.trim();
            const normalizedPhone = formData.phoneNumber.trim() || undefined;

            if (isEditMode) {
                //Update DTO : password not allowed 
                const payload: UpdateUserRequest = {
                    email: normalizedEmail,
                    fullName: normalizedName,
                    phoneNumber: normalizedPhone,
                    enabled: formData.enabled,
                    roleId: roleId,
                    role: role,
                };
                await onSubmit(payload);
            } else {
                //create DTO: password required 
                const payload: CreateUserRequest = {
                    email: normalizedEmail,
                    password: formData.password,
                    fullName: normalizedName,
                    phoneNumber: normalizedPhone,
                    enabled: formData.enabled,
                    roleId: roleId,
                    role: role,
                };
                await onSubmit(payload);
            }
            //clear errors 
            setErrors({});
        } catch {
            // Parent handles API error banner/toast; we keep this like RoleForm 
        } finally {
            //stop spinner 
            setIsSubmitting(false);
        }
    };

    //pick the first error to show in banner.
    //prefer backend error banner otherwise show first validation error 
    const firstError =
        externalError ||
        Object.values(errors).find((msg) => typeof msg === "string" && msg.length > 0) || null;

    
    const disabled = isLoading || isSubmitting;

    //JSX 
    return (
        //on click handleSubmit runs 
        <form onSubmit={handleSubmit} className="space-y-4">
            {/* Errror banner  */}
            {firstError && (
                <div className="flex items-center gap-2 p-3 text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
                    <AlertCircle className="h-4 w-4" />
                    <span>{firstError}</span>
                </div>
            )}

            {/* Full name */}
            <div className="space-y-2">
                <Label htmlFor="fullName">
                    {t("form.fields.fullName")}
                    <span className="text-destructive ml-1">*</span>
                </Label>
                <Input
                    id="fullName"
                    value={formData.fullName}
                    onChange={(e) => setField("fullName", e.target.value)}
                    placeholder={t("form.placeholders.fullName")}
                    disabled={disabled}
                    className={errors.fullName ? "border-destructive" : ""}
                />
                {errors.fullName && <p className="text-sm text-destructive">{errors.fullName}</p>}
            </div>

            {/* Email */}
            <div className="space-y-2">
                <Label htmlFor="email">
                    {t("form.fields.email")}
                    <span className="text-destructive ml-1">*</span>
                </Label>
                <Input
                    id="email"
                    value={formData.email}
                    onChange={(e) => setField("email", e.target.value)}
                    placeholder={t("form.placeholders.email")}
                    disabled={disabled}
                    className={errors.email ? "border-destructive" : ""}
                />
                {errors.email && <p className="text-sm text-destructive">{errors.email}</p>}
            </div>

            {/* Phone */}
            <div className="space-y-2">
                <Label htmlFor="phoneNumber">{t("form.fields.phoneNumber")}</Label>
                <Input
                    id="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={(e) => setField("phoneNumber", e.target.value)}
                    placeholder={t("form.placeholders.phoneNumber")}
                    disabled={disabled}
                />
            </div>

            {/* Role (Radix/shadcn Select) */}
            <div className="space-y-2">
                <Label htmlFor="roleId">
                    {t("form.fields.role")}
                    <span className="text-destructive ml-1">*</span>
                </Label>

                <Select
                    value={formData.roleId}
                    onValueChange={(value) => setField("roleId", value)}
                    disabled={disabled}
                >
                    <SelectTrigger
                        className={`w-full ${errors.roleId ? "border-destructive" : ""}`}
                    >
                        <SelectValue
                            placeholder={
                                roles.length > 0
                                    ? t("form.placeholders.role")
                                    : t("form.placeholders.roleLoading")
                            }
                        />
                    </SelectTrigger>

                    <SelectContent>
                        {roles.map((r) => (
                            <SelectItem key={r.id} value={String(r.id)}>
                                {r.title}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>

                {errors.roleId && <p className="text-sm text-destructive">{errors.roleId}</p>}
            </div>

            {/* Enabled */}
            <div className="flex items-center gap-2">
                <input
                    id="enabled"
                    type="checkbox"
                    checked={formData.enabled}
                    onChange={(e) => setField("enabled", e.target.checked)}
                    disabled={disabled}
                />
                <Label htmlFor="enabled">{t("form.fields.enabled")}</Label>
            </div>

            {/* Create-only: password */}
            {!isEditMode && (
                <>
                    <div className="space-y-2">
                        <Label htmlFor="password">
                            {t("form.fields.password")}
                            <span className="text-destructive ml-1">*</span>
                        </Label>
                        <Input
                            id="password"
                            type="password"
                            value={formData.password}
                            onChange={(e) => setField("password", e.target.value)}
                            placeholder={t("form.placeholders.password")}
                            disabled={disabled}
                            className={errors.password ? "border-destructive" : ""}
                        />
                        {errors.password && <p className="text-sm text-destructive">{errors.password}</p>}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="confirmPassword">
                            {t("form.fields.confirmPassword")}
                            <span className="text-destructive ml-1">*</span>
                        </Label>
                        <Input
                            id="confirmPassword"
                            type="password"
                            value={formData.confirmPassword}
                            onChange={(e) => setField("confirmPassword", e.target.value)}
                            placeholder={t("form.placeholders.confirmPassword")}
                            disabled={disabled}
                            className={errors.confirmPassword ? "border-destructive" : ""}
                        />
                        {errors.confirmPassword && (
                            <p className="text-sm text-destructive">{errors.confirmPassword}</p>
                        )}
                    </div>
                </>
            )}

            {/* Buttons */}
            <div className="flex justify-end gap-2 pt-4">
                <Button type="button" variant="outline" onClick={onCancel} disabled={disabled}>
                    {tCommon("actions.cancel")}
                </Button>

                <Button type="submit" disabled={disabled}>
                    {disabled ? (
                        <>
                            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                            {tCommon("actions.saving")}
                        </>
                    ) : isEditMode ? (
                        tCommon("actions.update")
                    ) : (
                        tCommon("actions.create")
                    )}
                </Button>
            </div>
        </form>
    );
}
