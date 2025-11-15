// react
import { useState, type FormEvent } from "react";
// routing
import { Link } from "react-router-dom";
// icons
import { Eye, EyeOff, AlertCircle } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
// hooks
import { useAuth } from "../hooks/useAuth";
// types
import type { LoginCredentials } from "../types/auth.types";
import { cn } from "@/lib/utils";
// translations
import { useTranslation } from "react-i18next";
import { ROUTES } from "@/config/routes";

export function LoginForm() {
  const { t } = useTranslation();
  const { login, isLoading, error, clearError } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState<LoginCredentials>({
    email: "",
    password: "",
    rememberMe: false,
  });
  const [fieldErrors, setFieldErrors] = useState<{
    email?: string;
    password?: string;
  }>({});

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const validateForm = (): boolean => {
    const errors: { email?: string; password?: string } = {};

    if (!formData.email) {
      errors.email = t("auth:login.emailRequired");
    } else if (!validateEmail(formData.email)) {
      errors.email = t("auth:login.emailInvalid");
    }

    if (!formData.password) {
      errors.password = t("auth:login.passwordRequired");
    } else if (formData.password.length < 6) {
      errors.password = t("auth:login.passwordMinLength");
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    clearError();
    setFieldErrors({});

    if (!validateForm()) {
      return;
    }

    try {
      await login(formData);
    } catch (err) {
      // Error is handled by useAuth hook
      console.error("Login error:", err);
    }
  };

  const handleInputChange = (field: keyof LoginCredentials, value: string | boolean) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear field error when user starts typing
    if (fieldErrors[field as keyof typeof fieldErrors]) {
      setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    // Clear general error when user starts typing
    if (error) {
      clearError();
    }
  };

  const emailError = fieldErrors.email || (error?.field === "email" ? error.message : null);
  const passwordError = fieldErrors.password || (error?.field === "password" ? error.message : null);
  const generalError = error?.field === "general" ? error.message : null;

  return (
    <div className="w-full max-w-md">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* General Error Message */}
        {generalError && (
          <div
            className={cn(
              "flex items-center gap-2 p-3 rounded-md text-sm",
              "bg-destructive/10 text-destructive border border-destructive/20"
            )}
            role="alert"
          >
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>{generalError}</span>
          </div>
        )}

        {/* Email Field */}
        <div className="space-y-2">
          <Label htmlFor="email">{t("auth:login.email")}</Label>
          <Input
            id="email"
            type="email"
            placeholder={t("auth:login.emailPlaceholder")}
            value={formData.email}
            onChange={(e) => handleInputChange("email", e.target.value)}
            aria-invalid={!!emailError}
            aria-describedby={emailError ? "email-error" : undefined}
            disabled={isLoading}
            className={cn("mt-2", emailError && "border-destructive")}
          />
          {emailError && (
            <p
              id="email-error"
              className="text-sm text-destructive flex items-center gap-1"
              role="alert"
            >
              <AlertCircle className="h-3 w-3" />
              {emailError}
            </p>
          )}
        </div>

        {/* Password Field */}
        <div className="space-y-3">
          <Label htmlFor="password">{t("auth:login.password")}</Label>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              placeholder={t("auth:login.passwordPlaceholder")}
              value={formData.password}
              onChange={(e) => handleInputChange("password", e.target.value)}
              aria-invalid={!!passwordError}
              aria-describedby={passwordError ? "password-error" : undefined}
              disabled={isLoading}
              className={cn("mt-2 pr-10", passwordError && "border-destructive")}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors mt-1"
              aria-label={showPassword ? "Hide password" : "Show password"}
              tabIndex={-1}
            >
              {showPassword ? (
                <EyeOff className="h-4 w-4" />
              ) : (
                <Eye className="h-4 w-4" />
              )}
            </button>
          </div>
          {passwordError && (
            <p
              id="password-error"
              className="text-sm text-destructive flex items-center gap-1"
              role="alert"
            >
              <AlertCircle className="h-3 w-3" />
              {passwordError}
            </p>
          )}
        </div>

        {/* Remember Me & Forgot Password */}
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Checkbox
              id="remember-me"
              checked={formData.rememberMe}
              onCheckedChange={(checked) =>
                handleInputChange("rememberMe", checked === true)
              }
              disabled={isLoading}
            />
            <Label
              htmlFor="remember-me"
              className="text-sm font-normal cursor-pointer"
            >
              {t("auth:login.rememberMe")}
            </Label>
          </div>
          <Link
            to={ROUTES.auth.forgotPassword}
            className="text-sm text-primary hover:underline"
          >
            {t("auth:login.forgotPassword")}
          </Link>
        </div>

        {/* Submit Button */}
        <Button
          type="submit"
          className="w-full"
          disabled={isLoading}
        >
          {isLoading ? t("auth:login.loggingIn") : t("auth:login.loginButton")}
        </Button>
      </form>
    </div>
  );
}

