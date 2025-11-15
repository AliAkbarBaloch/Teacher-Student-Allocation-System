import { useState, type FormEvent, useEffect } from "react";
// components
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
// translations
import { useTranslation } from "react-i18next";
// routing
import { Link, useSearchParams } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { cn } from "@/lib/utils";
// icons
import { Eye, EyeOff, AlertCircle, CheckCircle2, ArrowLeft } from "lucide-react";

export function ChangePasswordForm() {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{
    password?: string;
    confirmPassword?: string;
  }>({});

  // Validate token on mount
  useEffect(() => {
    if (!token) {
      setError(t("auth:changePassword.invalidToken"));
    }
  }, [token, t]);

  const validateForm = (): boolean => {
    const errors: { password?: string; confirmPassword?: string } = {};

    if (!password) {
      errors.password = t("auth:changePassword.passwordRequired");
    } else if (password.length < 6) {
      errors.password = t("auth:changePassword.passwordMinLength");
    }

    if (!confirmPassword) {
      errors.confirmPassword = t("auth:changePassword.confirmPasswordRequired");
    } else if (password !== confirmPassword) {
      errors.confirmPassword = t("auth:changePassword.passwordsDoNotMatch");
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setFieldErrors({});

    if (!token) {
      setError(t("auth:changePassword.invalidToken"));
      return;
    }

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      // Mock success - in real app, this would be an API call with the token
      setIsSuccess(true);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "An error occurred. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (field: "password" | "confirmPassword", value: string) => {
    if (field === "password") {
      setPassword(value);
    } else {
      setConfirmPassword(value);
    }

    // Clear field error when user starts typing
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    if (error) {
      setError(null);
    }
  };

  if (isSuccess) {
    return (
      <div className="w-full max-w-md space-y-6">
        <div
          className={cn(
            "flex items-start gap-3 p-4 rounded-md text-sm",
            "bg-green-50 dark:bg-green-950/20 text-green-800 dark:text-green-200 border border-green-200 dark:border-green-800"
          )}
        >
          <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-medium mb-1">{t("auth:changePassword.successMessage")}</p>
          </div>
        </div>
        <Link to={ROUTES.auth.login}>
          <Button className="w-full">
            {t("auth:changePassword.backToLogin")}
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="w-full max-w-md">
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* General Error Message */}
        {error && (
          <div
            className={cn(
              "flex items-center gap-2 p-3 rounded-md text-sm",
              "bg-destructive/10 text-destructive border border-destructive/20"
            )}
            role="alert"
          >
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Password Field */}
        <div className="space-y-2">
          <Label htmlFor="password">{t("auth:changePassword.password")}</Label>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              placeholder={t("auth:changePassword.passwordPlaceholder")}
              value={password}
              onChange={(e) => handleInputChange("password", e.target.value)}
              aria-invalid={!!fieldErrors.password}
              aria-describedby={fieldErrors.password ? "password-error" : undefined}
              disabled={isLoading || !token}
              className={cn(fieldErrors.password && "border-destructive", "pr-10")}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
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
          {fieldErrors.password && (
            <p
              id="password-error"
              className="text-sm text-destructive flex items-center gap-1"
              role="alert"
            >
              <AlertCircle className="h-3 w-3" />
              {fieldErrors.password}
            </p>
          )}
        </div>

        {/* Confirm Password Field */}
        <div className="space-y-2">
          <Label htmlFor="confirmPassword">{t("auth:changePassword.confirmPassword")}</Label>
          <div className="relative">
            <Input
              id="confirmPassword"
              type={showConfirmPassword ? "text" : "password"}
              placeholder={t("auth:changePassword.confirmPasswordPlaceholder")}
              value={confirmPassword}
              onChange={(e) => handleInputChange("confirmPassword", e.target.value)}
              aria-invalid={!!fieldErrors.confirmPassword}
              aria-describedby={fieldErrors.confirmPassword ? "confirm-password-error" : undefined}
              disabled={isLoading || !token}
              className={cn(fieldErrors.confirmPassword && "border-destructive", "pr-10")}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              aria-label={showConfirmPassword ? "Hide password" : "Show password"}
              tabIndex={-1}
            >
              {showConfirmPassword ? (
                <EyeOff className="h-4 w-4" />
              ) : (
                <Eye className="h-4 w-4" />
              )}
            </button>
          </div>
          {fieldErrors.confirmPassword && (
            <p
              id="confirm-password-error"
              className="text-sm text-destructive flex items-center gap-1"
              role="alert"
            >
              <AlertCircle className="h-3 w-3" />
              {fieldErrors.confirmPassword}
            </p>
          )}
        </div>

        {/* Submit Button */}
        <Button type="submit" className="w-full" disabled={isLoading || !token}>
          {isLoading ? t("auth:changePassword.changing") : t("auth:changePassword.submitButton")}
        </Button>

        {/* Back to Login Link */}
        <div className="text-center">
          <Link
            to={ROUTES.auth.login}
            className="text-sm text-primary hover:underline inline-flex items-center gap-1"
          >
            <ArrowLeft className="h-3 w-3" />
            {t("auth:changePassword.backToLogin")}
          </Link>
        </div>
      </form>
    </div>
  );
}

