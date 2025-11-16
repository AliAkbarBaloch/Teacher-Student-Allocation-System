import { useState, type FormEvent } from "react";
// components
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
// translations
import { useTranslation } from "react-i18next";
// routing
import { Link } from "react-router-dom";
import { ROUTES } from "@/config/routes";
// utils
import { cn } from "@/lib/utils";
// services
import { AuthService } from "../services/authService";
// icons
import { AlertCircle, CheckCircle2, ArrowLeft } from "lucide-react";

export function ForgotPasswordForm() {
  const { t } = useTranslation();
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldError, setFieldError] = useState<string | null>(null);

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setFieldError(null);

    // Validate email
    if (!email) {
      setFieldError(t("auth:forgotPassword.emailRequired"));
      return;
    }

    if (!validateEmail(email)) {
      setFieldError(t("auth:forgotPassword.emailInvalid"));
      return;
    }

    setIsLoading(true);

    try {
      await AuthService.forgotPassword({ email });
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

  const handleInputChange = (value: string) => {
    setEmail(value);
    if (fieldError) {
      setFieldError(null);
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
            <p className="font-medium mb-1">{t("auth:forgotPassword.submitSuccess")}</p>
            <p className="text-sm opacity-90">{t("auth:forgotPassword.successMessage")}</p>
          </div>
        </div>
        <Link to={ROUTES.auth.login}>
          <Button variant="outline" className="w-full">
            <ArrowLeft className="mr-2 h-4 w-4" />
            {t("auth:forgotPassword.backToLogin")}
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

        {/* Email Field */}
        <div className="space-y-2">
          <Label htmlFor="email">{t("auth:forgotPassword.email")}</Label>
          <Input
            id="email"
            type="email"
            placeholder={t("auth:forgotPassword.emailPlaceholder")}
            value={email}
            onChange={(e) => handleInputChange(e.target.value)}
            aria-invalid={!!fieldError}
            aria-describedby={fieldError ? "email-error" : undefined}
            disabled={isLoading}
            className={cn(fieldError && "border-destructive")}
          />
          {fieldError && (
            <p
              id="email-error"
              className="text-sm text-destructive flex items-center gap-1"
              role="alert"
            >
              <AlertCircle className="h-3 w-3" />
              {fieldError}
            </p>
          )}
        </div>

        {/* Submit Button */}
        <Button type="submit" className="w-full" disabled={isLoading}>
          {isLoading ? t("auth:forgotPassword.sending") : t("auth:forgotPassword.submitButton")}
        </Button>

        {/* Back to Login Link */}
        <div className="text-center">
          <Link
            to={ROUTES.auth.login}
            className="text-sm text-primary hover:underline inline-flex items-center gap-1"
          >
            <ArrowLeft className="h-3 w-3" />
            {t("auth:forgotPassword.backToLogin")}
          </Link>
        </div>
      </form>
    </div>
  );
}

