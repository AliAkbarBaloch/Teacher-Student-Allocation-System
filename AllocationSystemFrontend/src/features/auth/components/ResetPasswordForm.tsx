import { useState, type FormEvent, useEffect, useRef, useMemo } from "react";
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
// services
import { AuthService } from "../services/authService";
// utilities
import { calculatePasswordStrength } from "@/lib/passwordUtils";
// icons
import { Eye, EyeOff, AlertCircle, CheckCircle2, ArrowLeft, Check, X } from "lucide-react";

export function ResetPasswordForm() {
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
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [debouncedPassword, setDebouncedPassword] = useState("");

  // Debounce password strength calculation
  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      setDebouncedPassword(password);
    }, 300);

    // Cleanup timeout on unmount or when password changes
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [password]);

  const passwordStrength = useMemo(
    () => calculatePasswordStrength(debouncedPassword),
    [debouncedPassword]
  );

  // Validate token on mount
  useEffect(() => {
    if (!token) {
      setError(t("auth:resetPassword.invalidToken"));
    }
  }, [token, t]);

  const validateForm = (): boolean => {
    const errors: { password?: string; confirmPassword?: string } = {};

    if (!password) {
      errors.password = t("auth:resetPassword.passwordRequired");
    } else if (passwordStrength.score < 2) {
      errors.password = t("auth:resetPassword.passwordTooWeak");
    }

    if (!confirmPassword) {
      errors.confirmPassword = t("auth:resetPassword.confirmPasswordRequired");
    } else if (password !== confirmPassword) {
      errors.confirmPassword = t("auth:resetPassword.passwordsDoNotMatch");
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setFieldErrors({});

    if (!token) {
      setError(t("auth:resetPassword.invalidToken"));
      return;
    }

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      if (!token) {
        setError(t("auth:resetPassword.invalidToken"));
        return;
      }

      await AuthService.resetPassword({
        token,
        newPassword: password,
      });
      setIsSuccess(true);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "An error occurred. Please try again.";
      
      // Check if it's a token-related error
      const isTokenError = errorMessage.toLowerCase().includes("token") || 
                          errorMessage.toLowerCase().includes("invalid") ||
                          errorMessage.toLowerCase().includes("expired");
      
      if (isTokenError) {
        setError(errorMessage);
      } else {
        setFieldErrors((prev) => ({ ...prev, password: errorMessage }));
      }
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
            <p className="font-medium mb-1">{t("auth:resetPassword.successMessage")}</p>
          </div>
        </div>
        <Link to={ROUTES.auth.login}>
          <Button className="w-full">
            {t("auth:resetPassword.backToLogin")}
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
          <Label htmlFor="password">{t("auth:resetPassword.password")}</Label>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              placeholder={t("auth:resetPassword.passwordPlaceholder")}
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

          {/* Password Strength Indicator */}
          {password && (
            <div className="space-y-3 pt-3 mt-3 border-t animate-in fade-in slide-in-from-top-2 duration-300">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">{t("auth:resetPassword.passwordStrength")}</span>
                <span className={cn("font-medium", passwordStrength.color)}>
                  {t(`auth:resetPassword.strength.${passwordStrength.label}`)}
                </span>
              </div>
              <div className="h-2 bg-muted rounded-full overflow-hidden">
                <div
                  className={cn(
                    "h-full transition-all duration-300",
                    passwordStrength.score <= 1 && "bg-red-500",
                    passwordStrength.score === 2 && "bg-orange-500",
                    passwordStrength.score === 3 && "bg-yellow-500",
                    passwordStrength.score >= 4 && "bg-green-500"
                  )}
                  style={{ width: `${(passwordStrength.score / 5) * 100}%` }}
                />
              </div>
              <div className="space-y-1.5 text-sm">
                <div className={cn("flex items-center gap-2", passwordStrength.checks.minLength ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                  {passwordStrength.checks.minLength ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <X className="h-4 w-4" />
                  )}
                  <span>{t("auth:resetPassword.strength.minLength")}</span>
                </div>
                <div className={cn("flex items-center gap-2", passwordStrength.checks.hasUppercase ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                  {passwordStrength.checks.hasUppercase ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <X className="h-4 w-4" />
                  )}
                  <span>{t("auth:resetPassword.strength.hasUppercase")}</span>
                </div>
                <div className={cn("flex items-center gap-2", passwordStrength.checks.hasLowercase ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                  {passwordStrength.checks.hasLowercase ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <X className="h-4 w-4" />
                  )}
                  <span>{t("auth:resetPassword.strength.hasLowercase")}</span>
                </div>
                <div className={cn("flex items-center gap-2", passwordStrength.checks.hasNumber ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                  {passwordStrength.checks.hasNumber ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <X className="h-4 w-4" />
                  )}
                  <span>{t("auth:resetPassword.strength.hasNumber")}</span>
                </div>
                <div className={cn("flex items-center gap-2", passwordStrength.checks.hasSpecial ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                  {passwordStrength.checks.hasSpecial ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <X className="h-4 w-4" />
                  )}
                  <span>{t("auth:resetPassword.strength.hasSpecial")}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Confirm Password Field */}
        <div className="space-y-2">
          <Label htmlFor="confirmPassword">{t("auth:resetPassword.confirmPassword")}</Label>
          <div className="relative">
            <Input
              id="confirmPassword"
              type={showConfirmPassword ? "text" : "password"}
              placeholder={t("auth:resetPassword.confirmPasswordPlaceholder")}
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
          {isLoading ? t("auth:resetPassword.changing") : t("auth:resetPassword.submitButton")}
        </Button>

        {/* Back to Login Link */}
        <div className="text-center">
          <Link
            to={ROUTES.auth.login}
            className="text-sm text-primary hover:underline inline-flex items-center gap-1"
          >
            <ArrowLeft className="h-3 w-3" />
            {t("auth:resetPassword.backToLogin")}
          </Link>
        </div>
      </form>
    </div>
  );
}

